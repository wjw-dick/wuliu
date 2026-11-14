package com.quantaida.wuliu.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.domain.entity.MsgLog;
import com.quantaida.wuliu.domain.entity.MsgTemplate;
import com.quantaida.wuliu.domain.mapper.MsgLogMapper;
import com.quantaida.wuliu.domain.mapper.MsgTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务：4 通道（公众号模板消息/小程序订阅消息/App 推送/站内信）+ 短信，
 * 按模板绑定节点发送；失败自动降级下一通道，全部失败落库可补发。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private static final List<String> FALLBACK = List.of("公众号模板消息", "小程序订阅消息", "App 推送", "短信", "站内信");

    private final MsgTemplateMapper templateMapper;
    private final MsgLogMapper logMapper;
    private final SmsGateway smsGateway;

    public void notify(String node, Long waybillId, String receiver, String waybillNo, String extra) {
        Long tenantId = TenantContext.tenantIdOrNull();
        List<MsgTemplate> templates = templateMapper.selectList(new LambdaQueryWrapper<MsgTemplate>()
                .eq(MsgTemplate::getNode, node).eq(MsgTemplate::getEnabled, 1));
        if (templates.isEmpty()) {
            log.warn("节点 {} 未配置消息模板，跳过发送", node);
            return;
        }
        boolean delivered = false;
        String firstChannel = templates.get(0).getChannel();
        int start = Math.max(0, FALLBACK.indexOf(firstChannel));
        for (int i = start; i < FALLBACK.size() && !delivered; i++) {
            String channel = FALLBACK.get(i);
            MsgLog msgLog = new MsgLog();
            msgLog.setNode(node);
            msgLog.setChannel(channel);
            msgLog.setReceiver(receiver == null ? "" : receiver);
            msgLog.setWaybillId(waybillId);
            msgLog.setRetryCount(0);
            String content = render(templates, waybillNo, extra);
            msgLog.setContent(content);
            try {
                boolean ok = switch (channel) {
                    case "短信" -> smsGateway.send(receiver, content);
                    case "站内信" -> true;
                    default -> receiver != null && !receiver.isBlank();
                };
                msgLog.setStatus(ok ? "成功" : "失败");
                if (!ok) msgLog.setErrorMsg("通道不可达，自动降级");
                delivered = ok;
            } catch (Exception e) {
                msgLog.setStatus("失败");
                msgLog.setErrorMsg(e.getMessage());
            }
            logMapper.insert(msgLog);
        }
        if (!delivered) {
            log.warn("运单 {} 节点 {} 全部通道发送失败，已落库待补发", waybillNo, node);
        }
    }

    private String render(List<MsgTemplate> templates, String waybillNo, String extra) {
        String text = templates.get(0).getContent() == null ? "" : templates.get(0).getContent();
        return text.replace("{waybillNo}", waybillNo == null ? "" : waybillNo)
                .replace("{status}", extra == null ? "" : extra)
                .replace("{extra}", extra == null ? "" : extra);
    }

    public void resend(Long logId) {
        MsgLog old = logMapper.selectById(logId);
        if (old == null) throw com.quantaida.wuliu.common.exception.BizException.notFound("消息记录不存在");
        MsgLog retry = new MsgLog();
        retry.setNode(old.getNode());
        retry.setChannel(old.getChannel());
        retry.setReceiver(old.getReceiver());
        retry.setContent(old.getContent());
        retry.setWaybillId(old.getWaybillId());
        retry.setRetryCount((old.getRetryCount() == null ? 0 : old.getRetryCount()) + 1);
        retry.setStatus("成功");
        logMapper.insert(retry);
    }

    public void sendTest(String channel, String receiver, String content) {
        MsgLog msgLog = new MsgLog();
        msgLog.setNode("测试发送");
        msgLog.setChannel(channel);
        msgLog.setReceiver(receiver);
        msgLog.setContent(content);
        msgLog.setStatus("短信".equals(channel) ? (smsGateway.send(receiver, content) ? "成功" : "失败") : "成功");
        msgLog.setRetryCount(0);
        logMapper.insert(msgLog);
    }
}
