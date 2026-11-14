package com.quantaida.wuliu.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** 演示数据：2 个租户、角色账号、10 客户、20 运单（覆盖全状态）、车辆/司机/承运商、字典/菜单/模板/官网内容。 */
@Service
@RequiredArgsConstructor
public class SeedService {
    private final TenantMapper tenantMapper;
    private final TenantLicenseMapper licenseMapper;
    private final PlanMapper planMapper;
    private final PlatformUserMapper platformUserMapper;
    private final TenantUserMapper tenantUserMapper;
    private final TenantRoleMapper roleMapper;
    private final OrgMapper orgMapper;
    private final CustomerMapper customerMapper;
    private final ContactMapper contactMapper;
    private final AddressBookMapper addressMapper;
    private final AgreementMapper agreementMapper;
    private final PricingRuleMapper pricingRuleMapper;
    private final WaybillMapper waybillMapper;
    private final WaybillItemMapper itemMapper;
    private final WaybillStatusLogMapper statusLogMapper;
    private final VehicleMapper vehicleMapper;
    private final DriverMapper driverMapper;
    private final CarrierMapper carrierMapper;
    private final DispatchMapper dispatchMapper;
    private final SignRecordMapper signMapper;
    private final ReceiptMapper receiptMapper;
    private final ReceivableMapper receivableMapper;
    private final PayableMapper payableMapper;
    private final StatementMapper statementMapper;
    private final StatementItemMapper statementItemMapper;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;
    private final CodSettleMapper codMapper;
    private final ClaimMapper claimMapper;
    private final ComplaintMapper complaintMapper;
    private final ExceptionOrderMapper exceptionMapper;
    private final TrackNodeMapper trackNodeMapper;
    private final TrackPointMapper trackPointMapper;
    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;
    private final MenuMapper menuMapper;
    private final MsgTemplateMapper msgTemplateMapper;
    private final StorageConfigMapper storageConfigMapper;
    private final SsoConfigMapper ssoConfigMapper;
    private final WechatAccountMapper wechatMapper;
    private final I18nItemMapper i18nMapper;
    private final CmsContentMapper cmsMapper;
    private final SiteMapper siteMapper;
    private final SysConfigMapper sysConfigMapper;
    private final AnnouncementMapper announcementMapper;
    private final BillMapper billMapper;
    private final SeqHolder seq = new SeqHolder();

    public boolean isEmpty() {
        return tenantMapper.selectCount(new LambdaQueryWrapper<>()) == 0;
    }

    static class SeqHolder {
        int n = 0;

        String next() {
            return String.format("%04d", ++n);
        }
    }

    @Transactional
    public void seed() {
        if (!isEmpty()) return;
        seedPlans();
        Tenant t1 = seedTenant("DEMO", "示例物流（上海）有限公司", "91310000MA1FL00001", "正常", 20);
        Tenant t2 = seedTenant("DEMO2", "华南示例物流有限公司", "91440101MA5CX00002", "正常", 10);
        seedPlatformUsers();
        seedRolesAndUsers(t1);
        seedRolesAndUsers(t2);
        seedOrgs(t1);
        seedDictionaries(t1);
        seedMenus(t1);
        seedTemplates(t1);
        seedConfigs(t1);
        seedCms(t1);
        seedSites(t1);
        seedPricing(t1);
        seedPricing(t2);
        seedResources(t1);
        seedCustomers(t1);
        seedWaybills(t1, 20);
        seedWaybills(t2, 3);
        seedStatements(t1);
        seedClaims(t1);
        seedAnnouncements(t1);
        seedInfraConfigs(t1);
        seedBills(t1, t2);
        System.out.println("[seed] 演示数据初始化完成：租户 2 家、运单 " + waybillMapper.selectCount(new LambdaQueryWrapper<>()) + " 条");
    }

    private void seedPlans() {
        if (planMapper.selectCount(new LambdaQueryWrapper<>()) > 0) return;
        String[][] plans = {
                {"标准版", "10", "50", "2000", "2980"},
                {"专业版", "30", "200", "10000", "6980"},
                {"旗舰版", "100", "1000", "50000", "16800"}
        };
        for (String[] p : plans) {
            Plan plan = new Plan();
            plan.setName(p[0]);
            plan.setSeatLimit(Integer.parseInt(p[1]));
            plan.setStorageGb(Integer.parseInt(p[2]));
            plan.setSmsCount(Integer.parseInt(p[3]));
            plan.setPriceMonth(new BigDecimal(p[4]));
            plan.setFeatures("[\"全部业务模块\",\"多端账号\",\"数据报表\"]");
            plan.setStatus("启用");
            plan.setSortNo(planMapper.selectCount(new LambdaQueryWrapper<>()).intValue() + 1);
            planMapper.insert(plan);
        }
    }

    private Tenant seedTenant(String code, String name, String creditCode, String status, int seats) {
        Tenant tenant = new Tenant();
        tenant.setCode(code);
        tenant.setName(name);
        tenant.setCreditCode(creditCode);
        tenant.setIndustry("综合物流");
        tenant.setStatus(status);
        tenant.setPlanId(2L);
        tenant.setSeatLimit(seats);
        tenant.setSeatUsed(4);
        tenant.setNetworkMode(0);
        tenant.setContactName("王经理");
        tenant.setContactPhone("13800000000");
        tenant.setContactEmail("service@example.com");
        tenant.setTrialEnd(LocalDateTime.now().plusDays(15));
        tenant.setServiceStart(LocalDateTime.now().minusMonths(2));
        tenant.setServiceEnd(LocalDateTime.now().plusMonths(10));
        tenant.setRemark("演示租户");
        tenantMapper.insert(tenant);

        TenantLicense license = new TenantLicense();
        license.setTenantId(tenant.getId());
        license.setLicenseNo("沪交运管许可字 " + (310000 + tenant.getId()) + " 号");
        license.setLicenseExpire(LocalDateTime.now().plusYears(3));
        license.setBusinessLicenseUrl("/api/files/demo/business-license.pdf");
        license.setLegalIdUrl("/api/files/demo/legal-id.jpg");
        license.setStatus("已通过");
        license.setReviewedBy("平台运营");
        license.setReviewedAt(LocalDateTime.now().minusDays(20));
        licenseMapper.insert(license);
        return tenant;
    }

    private void seedPlatformUsers() {
        if (platformUserMapper.selectCount(new LambdaQueryWrapper<>()) > 0) return;
        createPlatformUser("admin", "平台超管", "平台超管");
        createPlatformUser("operation", "平台运营", "平台运营");
        createPlatformUser("pfinance", "平台财务", "平台财务");
    }

    private void createPlatformUser(String username, String nickname, String role) {
        PlatformUser user = new PlatformUser();
        user.setUsername(username);
        user.setPassword(AuthService.hash("admin123"));
        user.setNickname(nickname);
        user.setPhone("13900000000");
        user.setRole(role);
        user.setStatus("正常");
        platformUserMapper.insert(user);
    }

    private void seedRolesAndUsers(Tenant tenant) {
        TenantRole adminRole = role("企业管理员", "[\"*\"]", "[\"*\"]", "公司级", tenant.getId());
        TenantRole dispatchRole = role("调度", "[\"dashboard\",\"waybills\",\"dispatch\",\"vehicles\",\"drivers\",\"carriers\",\"tracks\",\"exceptions\"]",
                "[\"waybill:create\",\"dispatch:assign\",\"dispatch:reassign\"]", "公司级", tenant.getId());
        TenantRole financeRole = role("财务", "[\"dashboard\",\"receivables\",\"payables\",\"statements\",\"invoices\",\"payments\",\"aging\",\"cod\",\"reports\"]",
                "[\"statement:generate\",\"invoice:create\",\"payment:create\"]", "公司级", tenant.getId());
        TenantRole salesRole = role("销售", "[\"dashboard\",\"customers\",\"quotes\",\"leads\",\"waybills\"]",
                "[\"quote:create\",\"lead:assign\"]", "公司级", tenant.getId());
        role("客服", "[\"dashboard\",\"waybills\",\"tracks\",\"exceptions\",\"claims\",\"complaints\"]", "[\"exception:handle\"]", "公司级", tenant.getId());
        role("仓管", "[\"dashboard\",\"waybills\",\"dispatch\"]", "[]", "网点级", tenant.getId());
        role("内容运营", "[\"dashboard\",\"cms\",\"system\"]", "[]", "公司级", tenant.getId());
        role("审计只读", "[\"dashboard\",\"reports\",\"audits\"]", "[]", "公司级", tenant.getId());

        String suffix = "DEMO".equals(tenant.getCode()) ? "" : "2";
        createTenantUser(tenant, "admin" + suffix, "王经理" + (suffix.isEmpty() ? "" : "（华南）"), adminRole.getId(), "13800000001");
        if (suffix.isEmpty()) {
            createTenantUser(tenant, "dispatch", "李调度", dispatchRole.getId(), "13800000002");
            createTenantUser(tenant, "finance", "赵财务", financeRole.getId(), "13800000003");
            createTenantUser(tenant, "sales", "孙销售", salesRole.getId(), "13800000004");
        }
    }

    private TenantRole role(String name, String menus, String buttons, String scope, Long tenantId) {
        TenantRole role = new TenantRole();
        role.setName(name);
        role.setMenus(menus);
        role.setButtons(buttons);
        role.setDataScope(scope);
        role.setStatus("启用");
        role.setTenantId(tenantId);
        roleMapper.insert(role);
        return role;
    }

    private void createTenantUser(Tenant tenant, String username, String nickname, Long roleId, String phone) {
        TenantUser user = new TenantUser();
        user.setUsername(username);
        user.setPassword(AuthService.hash("admin123"));
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setRoleId(roleId);
        user.setStatus("正常");
        user.setTenantId(tenant.getId());
        tenantUserMapper.insert(user);
    }

    private void seedOrgs(Tenant tenant) {
        String[][] orgs = {{"总部", "公司"}, {"上海嘉定网点", "网点"}, {"杭州中转场", "网点"}, {"自有车队", "车队"}};
        for (String[] o : orgs) {
            Org org = new Org();
            org.setName(o[0]);
            org.setRecordType(o[1]);
            org.setParentId(0L);
            org.setAddress("上海市嘉定区物流大道 " + (seq.n + 1) + " 号");
            org.setPhone("021-6000000" + seq.next().substring(3));
            org.setStatus("启用");
            org.setTenantId(tenant.getId());
            orgMapper.insert(org);
        }
    }

    private void seedDictionaries(Tenant tenant) {
        Map<String, String[]> dicts = new LinkedHashMap<>();
        dicts.put("运单状态", new String[]{"待报价", "待客户确认", "待支付", "已受理", "待调度", "已派车", "已提货", "运输中", "中转中", "到达待派送", "派送中", "已签收", "待对账", "已对账待开票", "已结算", "已完结", "异常处理中", "退回中", "已退回", "理赔中", "已取消"});
        dicts.put("客户类型", new String[]{"企业客户", "个体客户", "月结客户", "平台会员"});
        dicts.put("结算方式", new String[]{"现结", "到付", "月结", "预付定金"});
        dicts.put("提货方式", new String[]{"上门提货", "客户自送到站"});
        dicts.put("配送方式", new String[]{"送货上门", "收货人自提", "送指定驿站"});
        dicts.put("车型", new String[]{"面包车", "4.2 米厢式", "6.8 米厢式", "9.6 米厢式", "13 米高栏", "平板车", "冷藏车", "危险品专用车"});
        dicts.put("货物类型", new String[]{"普货", "大件", "易碎品", "需冷藏", "需防震", "精密设备", "文件票据"});
        dicts.put("计费方式", new String[]{"按重量", "按体积", "按重量体积取大", "按公里", "按票", "最低一票", "包车"});
        dicts.put("增值服务", new String[]{"装卸", "上楼", "拆包装", "打木架", "保险", "代收货款", "回单返回", "签收回单原件"});
        dicts.put("异常类型", new String[]{"延误", "破损", "丢失", "短少", "拒收", "改址", "无人收货", "车辆故障", "交通事故", "扣押", "天气不可抗力"});
        dicts.put("责任方", new String[]{"承运商", "自有司机", "装卸方", "客户包装不当", "不可抗力", "待定"});
        dicts.put("结算状态", new String[]{"待生成", "待对账", "客户已确认", "有异议", "已开票", "部分收款", "已结清", "已挂账"});
        dicts.put("发票类型", new String[]{"增值税专用发票", "增值税普通发票", "电子普通发票", "不开发票"});
        dicts.put("支付方式", new String[]{"微信支付", "银行转账", "现金", "月结记账", "代收货款抵扣"});
        dicts.put("派车来源", new String[]{"自有车辆", "外协承运商", "临时外调"});
        dicts.put("理赔状态", new String[]{"待立案", "已立案", "定责中", "待审批", "已赔付", "已结案", "驳回"});
        dicts.put("承运商状态", new String[]{"待审核", "合作中", "已暂停", "已终止"});
        dicts.put("资质审核状态", new String[]{"待审核", "已通过", "已驳回", "已过期"});
        dicts.put("客户来源", new String[]{"官网询价", "公众号", "小程序", "电话", "销售开发", "老客推荐"});
        int sort = 1;
        for (Map.Entry<String, String[]> e : dicts.entrySet()) {
            DictType type = new DictType();
            type.setCode("D" + (sort < 10 ? "0" : "") + sort);
            type.setName(e.getKey());
            type.setSortNo(sort);
            type.setStatus("启用");
            type.setTenantId(tenant.getId());
            dictTypeMapper.insert(type);
            int itemSort = 1;
            for (String item : e.getValue()) {
                DictItem di = new DictItem();
                di.setTypeCode(type.getCode());
                di.setCode(item);
                di.setName(item);
                di.setSortNo(itemSort++);
                di.setStatus("启用");
                di.setTenantId(tenant.getId());
                dictItemMapper.insert(di);
            }
            sort++;
        }
    }

    private void seedMenus(Tenant tenant) {
        String[][] navs = {
                {"官网导航", "首页", "/"}, {"官网导航", "在线询价", "/quote"}, {"官网导航", "运单查询", "/track"},
                {"官网导航", "服务与价格", "/services"}, {"官网导航", "网点查询", "/sites"}, {"官网导航", "企业月结", "/monthly"},
                {"官网导航", "资讯公告", "/news"}, {"官网导航", "关于我们", "/about"},
                {"小程序 tabBar", "首页", "/pages/index/index"}, {"小程序 tabBar", "查价", "/pages/quote/quote"},
                {"小程序 tabBar", "订单", "/pages/orders/orders"}, {"小程序 tabBar", "我的", "/pages/profile/profile"},
                {"H5 底部导航", "首页", "/"}, {"H5 底部导航", "询价", "/quote-share"}, {"H5 底部导航", "我的", "/profile"},
                {"公众号菜单", "查价", "/quote"}, {"公众号菜单", "下单", "/order"}, {"公众号菜单", "查轨迹", "/track"},
                {"公众号菜单", "对账", "/statement"}, {"公众号菜单", "理赔", "/claim"}, {"公众号菜单", "网点", "/sites"}
        };
        int sort = 1;
        for (String[] n : navs) {
            Menu menu = new Menu();
            menu.setChannel(n[0]);
            menu.setName(n[1]);
            menu.setPath(n[2]);
            menu.setSortNo(sort++);
            menu.setParentId(0L);
            menu.setPayload("{\"name\":\"" + n[1] + "\",\"path\":\"" + n[2] + "\"}");
            menu.setStatus("启用");
            menu.setTenantId(tenant.getId());
            menuMapper.insert(menu);
        }
    }

    private void seedTemplates(Tenant tenant) {
        String[][] nodes = {
                {"下单成功", "公众号模板消息", "下单成功通知", "您的运单 {waybillNo} 已受理，当前状态：{status}"},
                {"派车通知", "公众号模板消息", "派车通知", "运单 {waybillNo} 已派车，司机将联系您"},
                {"到达通知", "小程序订阅消息", "到达通知", "运单 {waybillNo} 已到达目的站，即将派送"},
                {"签收通知", "公众号模板消息", "签收通知", "运单 {waybillNo} 已签收，感谢使用"},
                {"对账通知", "小程序订阅消息", "对账通知", "运单 {waybillNo} 对账单已生成，请确认"},
                {"理赔进度", "短信", "理赔进度", "运单 {waybillNo} 理赔进度：{status}"},
                {"超时提醒", "站内信", "超时提醒", "运单 {waybillNo}：{status}"},
                {"改价确认", "小程序订阅消息", "改价确认", "运单 {waybillNo} 重量差异需确认新价 {status}"},
                {"异常通知", "短信", "异常通知", "运单 {waybillNo} 异常：{status}"},
                {"回单完成", "站内信", "回单完成", "运单 {waybillNo} 回单已审核通过"},
                {"空驶费", "站内信", "空驶费通知", "运单 {waybillNo} 产生空驶费：{status}"},
                {"完结通知", "站内信", "完结通知", "运单 {waybillNo} 已完结"}
        };
        for (String[] n : nodes) {
            MsgTemplate t = new MsgTemplate();
            t.setNode(n[0]);
            t.setChannel(n[1]);
            t.setTemplateId("TPL_" + Math.abs(n[0].hashCode()));
            t.setTitle(n[2]);
            t.setContent(n[3]);
            t.setSmsChannel("阿里云");
            t.setEnabled(1);
            t.setTenantId(tenant.getId());
            msgTemplateMapper.insert(t);
        }
    }

    private void seedConfigs(Tenant tenant) {
        String[][] configs = {
                {"siteName", "示例物流 · 数字化货运平台", "基础设置"},
                {"servicePhone", "400-888-0000", "基础设置"},
                {"workTime", "周一至周日 08:00-20:00", "基础设置"},
                {"waybillPrefix", "YH", "单号规则"},
                {"pickupLeadHours", "2", "时效设置"},
                {"deliveryLeadHours", "24", "时效设置"},
                {"invoiceTitle", "示例物流（上海）有限公司", "财务设置"},
                {"mapProvider", "高德地图", "第三方"},
                {"mapKey", "demo-map-key", "第三方"},
                {"payChannel", "微信支付", "第三方"},
                {"securityExportConfirm", "true", "安全设置"}
        };
        for (String[] c : configs) {
            SysConfig config = new SysConfig();
            config.setConfigKey(c[0]);
            config.setConfigValue(c[1]);
            config.setGroupName(c[2]);
            config.setTenantId(tenant.getId());
            sysConfigMapper.insert(config);
        }
    }

    private void seedCms(Tenant tenant) {
        String[][] contents = {
                {"资讯", "零担专线如何做到次日达？", "从场站分拨到末端派送，时效提升的三个关键动作。", "<h2>时效提升的三个关键动作</h2><p>一是固定发车班次，二是中转场扫码交接，三是末端预约派送。</p>", "已发布"},
                {"资讯", "体积重换算口径说明", "轻抛货如何计费？一篇文章讲清体积重。", "<p>体积重 = 体积(m³) × 250kg。取实际重量与体积重的较大值计费。</p>", "已发布"},
                {"公告", "2026 年国庆期间时效安排", "国庆期间部分线路时效顺延 1 天。", "<p>10 月 1 日至 3 日，部分线路发车班次调整，请提前安排。</p>", "已发布"},
                {"服务介绍", "整车包车服务", "门到门整车运输，专人跟车。", "<p>提供 4.2 米至 13 米全车型包车，含装卸与保险增值服务。</p>", "已发布"},
                {"服务介绍", "同城配送服务", "半日达、次日达同城配送。", "<p>覆盖上海全域，支持代收货款与电子回单。</p>", "已发布"}
        };
        for (String[] c : contents) {
            CmsContent content = new CmsContent();
            content.setContentType(c[0]);
            content.setTitle(c[1]);
            content.setSummary(c[2]);
            content.setContent(c[3]);
            content.setLang("zh-CN");
            content.setStatus(c[4]);
            content.setSeoTitle(c[1]);
            content.setSeoKeywords("物流,货运,零担," + c[0]);
            content.setPublishedAt(LocalDateTime.now().minusDays(contents.length));
            content.setTenantId(tenant.getId());
            cmsMapper.insert(content);
        }
    }

    private void seedSites(Tenant tenant) {
        String[][] sites = {
                {"上海嘉定总部", "上海市嘉定区物流大道 88 号", "021-60000001", "08:00-20:00", "31.3835", "121.2500"},
                {"上海浦东网点", "上海市浦东新区川沙路 199 号", "021-60000002", "08:00-19:00", "31.1900", "121.6900"},
                {"杭州中转场", "杭州市余杭区物流园区 6 号", "0571-80000001", "07:00-22:00", "30.4200", "120.3000"},
                {"苏州网点", "苏州市相城区春申湖路 18 号", "0512-60000001", "08:00-19:00", "31.3700", "120.6400"}
        };
        for (String[] s : sites) {
            Site site = new Site();
            site.setName(s[0]);
            site.setAddress(s[1]);
            site.setPhone(s[2]);
            site.setBusinessHours(s[3]);
            site.setLat(new BigDecimal(s[4]));
            site.setLng(new BigDecimal(s[5]));
            site.setServiceArea("上海市、杭州市、苏州市及周边");
            site.setStatus("营业中");
            site.setTenantId(tenant.getId());
            siteMapper.insert(site);
        }
    }

    private void seedPricing(Tenant tenant) {
        PricingRule rule = new PricingRule();
        rule.setName("标准计费规则");
        rule.setVersion("V1");
        rule.setStatus("已生效");
        rule.setEffectiveFrom(LocalDateTime.now().minusMonths(1));
        rule.setConfigJson("""
                {"mode":"按重量体积取大","volumeFactor":250,"firstWeight":100,"firstPrice":120,
                 "addWeightPrice":1.2,"pricePerCbm":300,"pricePerKm":6,"pricePerTicket":300,
                 "minPrice":200,"fuelRate":0.10,"loadingFee":50,"upstairsFee":30,
                 "insuranceRate":0.003,"codRate":0.006,"remoteFee":20,"holidayFee":50,
                 "routes":[
                   {"origin":"上海","destination":"杭州","vehicleType":"6.8 米厢式","price":600},
                   {"origin":"上海","destination":"苏州","vehicleType":"4.2 米厢式","price":380},
                   {"origin":"上海","destination":"北京","vehicleType":"9.6 米厢式","price":2600},
                   {"origin":"杭州","destination":"广州","vehicleType":"13 米高栏","price":4800}
                 ],
                 "distances":{"上海-杭州":180,"上海-苏州":100,"上海-北京":1200,"杭州-广州":1300}}
                """);
        rule.setRemark("首重 100kg/120 元，续重 1.2 元/kg，燃油 10%");
        rule.setTenantId(tenant.getId());
        pricingRuleMapper.insert(rule);

        PricingRule draft = new PricingRule();
        draft.setName("2027 年价目（草稿）");
        draft.setVersion("V2");
        draft.setStatus("草稿");
        draft.setEffectiveFrom(LocalDateTime.now().plusMonths(2));
        draft.setConfigJson(rule.getConfigJson());
        draft.setTenantId(tenant.getId());
        pricingRuleMapper.insert(draft);
    }

    private void seedResources(Tenant tenant) {
        Carrier carrier = new Carrier();
        carrier.setName("杭州快捷运输有限公司");
        carrier.setLicenseNo("浙交运管许可字 3301000001 号");
        carrier.setLicenseExpire(LocalDateTime.now().plusMonths(8));
        carrier.setSettlePriceJson("{\"按趟\":1200,\"按重量\":0.8}");
        carrier.setScore(new BigDecimal("4.80"));
        carrier.setStatus("合作中");
        carrier.setContactName("周师傅");
        carrier.setContactPhone("13900002001");
        carrier.setTenantId(tenant.getId());
        carrierMapper.insert(carrier);

        String[][] vehicles = {
                {"沪A12345", "6.8 米厢式", "8.00", "42.00", "10", "90"},
                {"沪A23456", "4.2 米厢式", "3.00", "18.00", "180", "200"},
                {"沪A34567", "9.6 米厢式", "15.00", "60.00", "300", "260"},
                {"沪A45678", "13 米高栏", "30.00", "90.00", "400", "330"},
                {"沪A56789", "冷藏车", "10.00", "50.00", "-10", "120"}
        };
        List<Vehicle> vs = new ArrayList<>();
        for (String[] v : vehicles) {
            Vehicle vehicle = new Vehicle();
            vehicle.setPlateNo(v[0]);
            vehicle.setVehicleType(v[1]);
            vehicle.setTonnage(new BigDecimal(v[2]));
            vehicle.setVolume(new BigDecimal(v[3]));
            vehicle.setTransportNo("沪道运证 " + Math.abs(v[0].hashCode() % 100000));
            vehicle.setTransportExpire(LocalDateTime.now().plusDays(Integer.parseInt(v[4])));
            vehicle.setInsuranceExpire(LocalDateTime.now().plusDays(Integer.parseInt(v[5])));
            vehicle.setFleet("自有车队");
            vehicle.setStatus("正常");
            vehicle.setCurrentStatus("空闲");
            vehicle.setCurrentLat(new BigDecimal("31.2304"));
            vehicle.setCurrentLng(new BigDecimal("121.4737"));
            vehicle.setTenantId(tenant.getId());
            vehicleMapper.insert(vehicle);
            vs.add(vehicle);
        }
        String[][] drivers = {
                {"张师傅", "13800001001", "10", "20", "0"},
                {"李师傅", "13800001002", "200", "300", "1"},
                {"王师傅", "13800001003", "400", "500", "2"},
                {"赵师傅", "13800001004", "600", "700", "3"},
                {"陈师傅", "13800001005", "-5", "30", "4"}
        };
        for (String[] d : drivers) {
            Driver driver = new Driver();
            driver.setName(d[0]);
            driver.setPhone(d[1]);
            driver.setLicenseNo("310000" + d[1].substring(7));
            driver.setLicenseExpire(LocalDateTime.now().plusDays(Integer.parseInt(d[2])));
            driver.setQualificationNo("沪从业 " + d[1].substring(7));
            driver.setQualificationExpire(LocalDateTime.now().plusDays(Integer.parseInt(d[3])));
            driver.setVehicleId(vs.get(Integer.parseInt(d[4])).getId());
            driver.setStatus("在职");
            driver.setTenantId(tenant.getId());
            driverMapper.insert(driver);
        }
    }

    private void seedCustomers(Tenant tenant) {
        String[][] customers = {
                {"上海示例制造有限公司", "企业客户", "月结", "50000", "48000", "张经理", "13800000001", "上海"},
                {"杭州商贸有限公司", "企业客户", "月结", "100000", "23000", "李经理", "13800000002", "杭州"},
                {"苏州精密仪器厂", "企业客户", "现结", "0", "0", "周经理", "13800000003", "苏州"},
                {"上海家居批发部", "企业客户", "到付", "0", "0", "吴经理", "13800000004", "上海"},
                {"义乌小商品电商仓", "月结客户", "月结", "30000", "12000", "郑经理", "13800000005", "义乌"},
                {"北京机械设备有限公司", "企业客户", "月结", "80000", "15000", "冯经理", "13800000006", "北京"},
                {"广州食品贸易有限公司", "企业客户", "现结", "0", "0", "陈经理", "13800000007", "广州"},
                {"南京电子元件商行", "个体客户", "到付", "0", "0", "许先生", "13800000008", "南京"},
                {"无锡纺织有限公司", "企业客户", "月结", "20000", "19000", "何经理", "13800000009", "无锡"},
                {"上海跨境电商仓", "平台会员", "预付定金", "0", "0", "林经理", "13800000010", "上海"}
        };
        int i = 1;
        for (String[] c : customers) {
            Customer customer = new Customer();
            customer.setCode("KH" + seq.next());
            customer.setName(c[0]);
            customer.setRecordType(c[1]);
            customer.setLevel(i <= 3 ? "战略客户" : i <= 7 ? "重点客户" : "普通客户");
            customer.setContactName(c[5]);
            customer.setContactPhone(c[6]);
            customer.setSettleType(c[2]);
            customer.setCreditLimit(new BigDecimal(c[3]));
            customer.setCreditUsed(new BigDecimal(c[4]));
            customer.setAccountPeriodDays("月结".equals(c[2]) ? 30 : 0);
            customer.setSource(i % 2 == 0 ? "官网询价" : "销售开发");
            customer.setAddress(c[7] + "市示例路 " + (10 + i) + " 号");
            customer.setStatus("合作中");
            customer.setRemark("演示客户");
            customer.setTenantId(tenant.getId());
            customerMapper.insert(customer);

            Contact contact = new Contact();
            contact.setCustomerId(customer.getId());
            contact.setName(c[5]);
            contact.setPhone(c[6]);
            contact.setTitle("物流负责人");
            contact.setIsDefault(1);
            contact.setTenantId(tenant.getId());
            contactMapper.insert(contact);

            AddressBook address = new AddressBook();
            address.setCustomerId(customer.getId());
            address.setName(c[5]);
            address.setPhone(c[6]);
            address.setRegion(c[7]);
            address.setDetail("示例路 " + (10 + i) + " 号 " + (i + 1) + " 号库");
            address.setRecordType("发货");
            address.setIsDefault(1);
            address.setTenantId(tenant.getId());
            addressMapper.insert(address);
            i++;
        }

        List<Customer> list = customerMapper.selectList(new LambdaQueryWrapper<>());
        String[][] agreements = {
                {"上海", "杭州", "6.8 米厢式", "560", "500"},
                {"上海", "苏州", "4.2 米厢式", "350", "320"},
                {"杭州", "广州", "13 米高栏", "4500", "4200"}
        };
        for (Customer customer : list.subList(0, 3)) {
            for (String[] a : agreements) {
                Agreement agreement = new Agreement();
                agreement.setCustomerId(customer.getId());
                agreement.setOrigin(a[0]);
                agreement.setDestination(a[1]);
                agreement.setVehicleType(a[2]);
                agreement.setPrice(new BigDecimal(a[3]));
                agreement.setMinPrice(new BigDecimal(a[4]));
                agreement.setValidFrom(LocalDateTime.now().minusMonths(1));
                agreement.setValidTo(LocalDateTime.now().plusMonths(6));
                agreement.setStatus("生效中");
                agreement.setApprover("王经理");
                agreement.setTenantId(tenant.getId());
                agreementMapper.insert(agreement);
            }
        }
    }

    private void seedWaybills(Tenant tenant, int count) {
        String[] statuses = {"待报价", "待客户确认", "待支付", "已受理", "待调度", "已派车", "已提货", "运输中",
                "到达待派送", "派送中", "已签收", "待对账", "已对账待开票", "已结算", "已完结",
                "异常处理中", "退回中", "已退回", "理赔中", "已取消"};
        List<Customer> customers = customerMapper.selectList(new LambdaQueryWrapper<>());
        List<Vehicle> vehicles = vehicleMapper.selectList(new LambdaQueryWrapper<>());
        List<Driver> drivers = driverMapper.selectList(new LambdaQueryWrapper<>());
        Carrier carrier = carrierMapper.selectOne(new LambdaQueryWrapper<Carrier>().last("limit 1"));
        String[][] lines = {{"上海", "杭州"}, {"上海", "苏州"}, {"上海", "北京"}, {"杭州", "广州"}, {"苏州", "上海"}};
        for (int i = 0; i < count; i++) {
            int idx = i % customers.size();
            Customer customer = customers.get(idx);
            String[] line = lines[i % lines.length];
            Waybill wb = new Waybill();
            wb.setTenantId(tenant.getId());
            wb.setWaybillNo("YH" + String.format("%04d", tenant.getId()) + LocalDateTime.now().minusDays(10).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", i + 1));
            wb.setCustomerId(customer.getId());
            wb.setCustomerName(customer.getName());
            wb.setSenderName(customer.getContactName());
            wb.setSenderPhone(customer.getContactPhone());
            wb.setSenderAddress(customer.getAddress());
            wb.setReceiverName("收货人" + (i + 1));
            wb.setReceiverPhone("1370000" + String.format("%04d", 1000 + i));
            wb.setReceiverAddress(line[1] + "市收货路 " + (20 + i) + " 号");
            wb.setOrigin(line[0]);
            wb.setDestination(line[1]);
            wb.setCargoName(i % 3 == 0 ? "机械配件" : i % 3 == 1 ? "家居用品" : "电子元件");
            wb.setCargoType("普货");
            wb.setWeight(new BigDecimal(200 + i * 30));
            wb.setVolume(new BigDecimal("1.2").add(new BigDecimal(i).multiply(new BigDecimal("0.2"))));
            wb.setPieces(5 + i);
            wb.setVehicleType(i % 2 == 0 ? "6.8 米厢式" : "4.2 米厢式");
            wb.setPickupType("上门提货");
            wb.setDeliveryType("送货上门");
            wb.setSettleType(customer.getSettleType());
            wb.setPickupTime(LocalDateTime.now().minusDays(5).plusHours(i));
            wb.setDeliveryTime(LocalDateTime.now().minusDays(4).plusHours(i));
            wb.setStatus(statuses[i % statuses.length]);
            wb.setBaseFreight(new BigDecimal("600.00"));
            wb.setFuelSurcharge(new BigDecimal("60.00"));
            wb.setLoadingFee(new BigDecimal("50.00"));
            wb.setUpstairsFee(BigDecimal.ZERO);
            wb.setInsuranceFee(new BigDecimal("30.00"));
            wb.setCodFee(BigDecimal.ZERO);
            wb.setRemoteFee(BigDecimal.ZERO);
            wb.setHolidayFee(BigDecimal.ZERO);
            wb.setTotalAmount(new BigDecimal("740.00"));
            wb.setReceivableAmount(new BigDecimal("740.00"));
            wb.setPayableAmount(new BigDecimal("520.00"));
            wb.setGrossProfit(new BigDecimal("220.00"));
            wb.setInsured(1);
            wb.setCodAmount(i % 4 == 0 ? new BigDecimal("2000.00") : BigDecimal.ZERO);
            wb.setRuleVersion("V1");
            wb.setSource(i % 2 == 0 ? "小程序" : "后台代录");
            wb.setCreatedBy("演示数据");
            wb.setRemark("演示运单 " + (i + 1));
            wb.setCreatedAt(LocalDateTime.now().minusDays(10).plusDays(i % 10));
            wb.setUpdatedAt(LocalDateTime.now().minusHours(i));
            wb.setAcceptedAt(wb.getCreatedAt().plusHours(1));
            String status = wb.getStatus();
            boolean dispatched = List.of("已派车", "已提货", "运输中", "中转中", "到达待派送", "派送中", "已签收",
                    "待对账", "已对账待开票", "已结算", "已完结", "异常处理中", "退回中", "已退回", "理赔中").contains(status);
            if (dispatched) {
                Vehicle v = vehicles.get(i % vehicles.size());
                Driver d = drivers.get(i % drivers.size());
                wb.setVehicleType(v.getVehicleType());
                wb.setDriverId(d.getId());
                wb.setDriverName(d.getName());
                wb.setDriverPhone(d.getPhone());
                wb.setPlateNo(v.getPlateNo());
                if (i % 3 == 0) {
                    wb.setCarrierId(carrier.getId());
                    wb.setCarrierName(carrier.getName());
                }
            }
            boolean signed = List.of("已签收", "待对账", "已对账待开票", "已结算", "已完结", "理赔中").contains(status);
            if (signed) {
                wb.setSignedAt(wb.getDeliveryTime().plusHours(2));
            }
            if ("已完结".equals(status)) wb.setFinishedAt(wb.getSignedAt().plusDays(1));
            waybillMapper.insert(wb);

            WaybillItem item = new WaybillItem();
            item.setTenantId(tenant.getId());
            item.setWaybillId(wb.getId());
            item.setName(wb.getCargoName());
            item.setQuantity(wb.getPieces());
            item.setWeight(wb.getWeight());
            item.setVolume(wb.getVolume());
            itemMapper.insert(item);

            WaybillStatusLog log = new WaybillStatusLog();
            log.setTenantId(tenant.getId());
            log.setWaybillId(wb.getId());
            log.setFromStatus("");
            log.setToStatus(status);
            log.setAction("初始化");
            log.setOperator("演示数据");
            log.setCreatedAt(wb.getCreatedAt());
            statusLogMapper.insert(log);

            if (dispatched) {
                Dispatch dispatch = new Dispatch();
                dispatch.setTenantId(tenant.getId());
                dispatch.setDispatchNo("PC" + String.format("%04d", tenant.getId()) + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", i + 1));
                dispatch.setWaybillId(wb.getId());
                dispatch.setVehicleId(wb.getDriverId() == null ? null : vehicles.get(i % vehicles.size()).getId());
                dispatch.setDriverId(wb.getDriverId());
                dispatch.setCarrierId(wb.getCarrierId());
                dispatch.setStatus("运输中".equals(status) || "已签收".equals(status) ? "已完成" : "已接单");
                dispatch.setSource(wb.getCarrierId() == null ? "自有车辆" : "外协承运商");
                dispatch.setAssignee("李调度");
                dispatch.setAssignedAt(wb.getCreatedAt().plusHours(2));
                dispatch.setAcceptedAt(wb.getCreatedAt().plusHours(3));
                dispatch.setEmptyFee(BigDecimal.ZERO);
                dispatchMapper.insert(dispatch);
            }

            if (List.of("运输中", "到达待派送", "派送中", "已签收", "待对账", "已对账待开票", "已结算", "已完结").contains(status)) {
                String[] nodes = {"到达发货地", "装车完成", "已发车", "到达目的站"};
                for (int n = 0; n < nodes.length; n++) {
                    TrackNode node = new TrackNode();
                    node.setTenantId(tenant.getId());
                    node.setWaybillId(wb.getId());
                    node.setNode(nodes[n]);
                    node.setLat(new BigDecimal("31.2304").add(new BigDecimal(n).multiply(new BigDecimal("0.1"))));
                    node.setLng(new BigDecimal("121.4737").add(new BigDecimal(n).multiply(new BigDecimal("0.1"))));
                    node.setOperator(wb.getDriverName() == null ? "场站" : wb.getDriverName());
                    node.setCreatedAt(wb.getCreatedAt().plusHours(4 + n * 2));
                    trackNodeMapper.insert(node);
                }
            }
            if ("运输中".equals(status)) {
                for (int p = 0; p < 5; p++) {
                    TrackPoint point = new TrackPoint();
                    point.setTenantId(tenant.getId());
                    point.setWaybillId(wb.getId());
                    point.setVehicleId(wb.getDriverId());
                    point.setLat(new BigDecimal("31.2304").add(new BigDecimal(p).multiply(new BigDecimal("0.05"))));
                    point.setLng(new BigDecimal("121.4737").add(new BigDecimal(p).multiply(new BigDecimal("0.05"))));
                    point.setSpeed(new BigDecimal("65.5"));
                    point.setReportedAt(LocalDateTime.now().minusMinutes(10 - p * 2));
                    point.setBucket(LocalDateTime.now().minusMinutes(10 - p * 2).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
                    trackPointMapper.insert(point);
                }
            }

            if (signed) {
                SignRecord sign = new SignRecord();
                sign.setTenantId(tenant.getId());
                sign.setWaybillId(wb.getId());
                sign.setSignerName("收货人" + (i + 1));
                sign.setSignerPhone(wb.getReceiverPhone());
                sign.setSignImage("/api/files/demo/sign-" + (i + 1) + ".png");
                sign.setSignedAt(wb.getSignedAt());
                signMapper.insert(sign);

                Receipt receipt = new Receipt();
                receipt.setTenantId(tenant.getId());
                receipt.setWaybillId(wb.getId());
                receipt.setImages("[\"/api/files/demo/receipt-" + (i + 1) + ".jpg\"]");
                receipt.setStatus("已通过");
                receipt.setReviewer("赵财务");
                receipt.setReviewedAt(wb.getSignedAt().plusHours(6));
                receiptMapper.insert(receipt);

                Receivable receivable = new Receivable();
                receivable.setTenantId(tenant.getId());
                receivable.setWaybillId(wb.getId());
                receivable.setWaybillNo(wb.getWaybillNo());
                receivable.setCustomerId(wb.getCustomerId());
                receivable.setCustomerName(wb.getCustomerName());
                receivable.setAmount(wb.getTotalAmount());
                receivable.setPaidAmount("已完结".equals(status) || "已结算".equals(status) ? wb.getTotalAmount() : BigDecimal.ZERO);
                receivable.setStatus(switch (status) {
                    case "已完结", "已结算" -> "已结清";
                    case "已对账待开票" -> "已开票";
                    case "理赔中" -> "已挂账";
                    default -> "待对账";
                });
                receivable.setSource("运单签收");
                receivable.setCreatedAt(wb.getSignedAt());
                receivableMapper.insert(receivable);

                Payable payable = new Payable();
                payable.setTenantId(tenant.getId());
                payable.setWaybillId(wb.getId());
                payable.setWaybillNo(wb.getWaybillNo());
                payable.setCarrierId(wb.getCarrierId());
                payable.setDriverId(wb.getDriverId());
                payable.setPayeeName(wb.getCarrierName() == null ? wb.getDriverName() : wb.getCarrierName());
                payable.setAmount(wb.getPayableAmount());
                payable.setPaidAmount("已完结".equals(status) ? wb.getPayableAmount() : BigDecimal.ZERO);
                payable.setStatus("已完结".equals(status) ? "已结清" : "待对账");
                payable.setCreatedAt(wb.getSignedAt());
                payableMapper.insert(payable);

                if (wb.getCodAmount() != null && wb.getCodAmount().signum() > 0) {
                    CodSettle cod = new CodSettle();
                    cod.setTenantId(tenant.getId());
                    cod.setWaybillId(wb.getId());
                    cod.setWaybillNo(wb.getWaybillNo());
                    cod.setAmount(wb.getCodAmount());
                    cod.setFee(wb.getCodFee());
                    cod.setStatus("已完结".equals(status) ? "已回款" : "待回款");
                    cod.setChannelNo("PAY" + seq.next());
                    codMapper.insert(cod);
                }
            }

            if ("异常处理中".equals(status)) {
                ExceptionOrder order = new ExceptionOrder();
                order.setTenantId(tenant.getId());
                order.setWaybillId(wb.getId());
                order.setRecordType("延误");
                order.setDescription("高速拥堵，预计延误 4 小时");
                order.setImages("[]");
                order.setStatus("待处理");
                order.setEscalated(0);
                exceptionMapper.insert(order);
            }
            if ("退回中".equals(status)) {
                com.quantaida.wuliu.domain.entity.ReturnTask task = new com.quantaida.wuliu.domain.entity.ReturnTask();
                task.setTenantId(tenant.getId());
                task.setWaybillId(wb.getId());
                task.setReason("收货人拒收：外包装破损");
                task.setDisposition("退回发货人");
                task.setReturnFee(new BigDecimal("300.00"));
                task.setStatus("退回中");
                returnTaskMapper.insert(task);
            }
        }
    }

    private final ReturnTaskMapper returnTaskMapper;

    private void seedStatements(Tenant tenant) {
        List<Receivable> receivables = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                .eq(Receivable::getStatus, "待对账"));
        if (receivables.isEmpty()) return;
        Statement statement = new Statement();
        statement.setTenantId(tenant.getId());
        statement.setStatementNo("DZ" + String.format("%04d", tenant.getId()) + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0001");
        statement.setRecordType("客户");
        statement.setCustomerId(receivables.get(0).getCustomerId());
        statement.setPartyName(receivables.get(0).getCustomerName());
        statement.setPeriodStart(LocalDateTime.now().minusMonths(1));
        statement.setPeriodEnd(LocalDateTime.now());
        BigDecimal total = receivables.stream().map(r -> r.getAmount() == null ? BigDecimal.ZERO : r.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setTotalAmount(total);
        statement.setConfirmedAmount(BigDecimal.ZERO);
        statement.setStatus("待客户确认");
        statement.setCreatedBy("赵财务");
        statementMapper.insert(statement);
        for (Receivable r : receivables) {
            StatementItem item = new StatementItem();
            item.setTenantId(tenant.getId());
            item.setStatementId(statement.getId());
            item.setWaybillId(r.getWaybillId());
            item.setWaybillNo(r.getWaybillNo());
            item.setAmount(r.getAmount());
            item.setDifference(BigDecimal.ZERO);
            statementItemMapper.insert(item);
            r.setStatementId(statement.getId());
            receivableMapper.updateById(r);
        }
    }

    private void seedClaims(Tenant tenant) {
        List<Waybill> signed = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .isNotNull(Waybill::getSignedAt).last("limit 3"));
        if (signed.size() < 2) return;
        Claim claim = new Claim();
        claim.setTenantId(tenant.getId());
        claim.setClaimNo("LP" + String.format("%04d", tenant.getId()) + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0001");
        claim.setWaybillId(signed.get(0).getId());
        claim.setWaybillNo(signed.get(0).getWaybillNo());
        claim.setRecordType("破损");
        claim.setDescription("外包装破损，内部货物轻微划伤");
        claim.setEvidence("[\"/api/files/demo/claim-1.jpg\",\"/api/files/demo/claim-2.jpg\"]");
        claim.setStatus("已立案");
        claim.setHandler("客服小李");
        claimMapper.insert(claim);

        Claim closed = new Claim();
        closed.setTenantId(tenant.getId());
        closed.setClaimNo("LP" + String.format("%04d", tenant.getId()) + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0002");
        closed.setWaybillId(signed.get(1).getId());
        closed.setWaybillNo(signed.get(1).getWaybillNo());
        closed.setRecordType("短少");
        closed.setDescription("到货短少 2 件");
        closed.setEvidence("[\"/api/files/demo/claim-3.jpg\"]");
        closed.setLiability("承运商");
        closed.setLiabilityReason("装卸交接未清点");
        closed.setAmount(new BigDecimal("380.00"));
        closed.setStatus("已结案");
        closed.setHandler("客服小李");
        closed.setClosedAt(LocalDateTime.now().minusDays(2));
        claimMapper.insert(closed);

        Complaint complaint = new Complaint();
        complaint.setTenantId(tenant.getId());
        complaint.setWaybillId(signed.get(0).getId());
        complaint.setCustomerId(signed.get(0).getCustomerId());
        complaint.setRecordType("服务态度");
        complaint.setContent("司机未按预约时间到达，未提前告知");
        complaint.setSatisfaction(3);
        complaint.setHandler("客服小李");
        complaint.setStatus("已回访");
        complaintMapper.insert(complaint);
    }

    private void seedAnnouncements(Tenant tenant) {
        Announcement platformAnn = new Announcement();
        platformAnn.setTenantId(0L);
        platformAnn.setTitle("平台 V1.0 上线通知");
        platformAnn.setContent("<p>物流货运数字化多端运营平台正式上线，支持零担/整车/城配全链路。</p>");
        platformAnn.setRecordType("版本通知");
        platformAnn.setStatus("已发布");
        platformAnn.setPublishedAt(LocalDateTime.now().minusDays(3));
        announcementMapper.insert(platformAnn);

        Announcement tenantAnn = new Announcement();
        tenantAnn.setTenantId(tenant.getId());
        tenantAnn.setTitle("2026 年 11 月价格调整通知");
        tenantAnn.setContent("<p>自 11 月 15 日起，上海至杭州线路燃油附加由 8% 调整为 10%。</p>");
        tenantAnn.setRecordType("价格公告");
        tenantAnn.setStatus("已发布");
        tenantAnn.setPublishedAt(LocalDateTime.now().minusDays(1));
        announcementMapper.insert(tenantAnn);
    }

    private void seedInfraConfigs(Tenant tenant) {
        StorageConfig storage = new StorageConfig();
        storage.setTenantId(tenant.getId());
        storage.setDriver("local");
        storage.setConfigJson("{\"baseDir\":\"./data/storage\",\"publicBaseUrl\":\"/api/files\"}");
        storage.setStatus("启用");
        storageConfigMapper.insert(storage);

        SsoConfig sso = new SsoConfig();
        sso.setTenantId(tenant.getId());
        sso.setProvider("OAuth2");
        sso.setConfigJson("{\"authorizeUrl\":\"\",\"clientId\":\"\",\"clientSecret\":\"\",\"userInfoUrl\":\"\"}");
        sso.setEnabled(0);
        ssoConfigMapper.insert(sso);

        WechatAccount wechat = new WechatAccount();
        wechat.setTenantId(tenant.getId());
        wechat.setAccountType("服务号");
        wechat.setAppId("wx_demo_service_account");
        wechat.setAppSecret("demo_app_secret");
        wechat.setServerUrl("https://admin.quantaida.cloud/api/wechat/callback");
        wechat.setToken("demo_token");
        wechat.setAesKey("demo_aes_key_0123456789012345678901");
        wechat.setDomains("admin.quantaida.cloud,www.quantaida.cloud");
        wechat.setIpWhitelist("127.0.0.1");
        wechat.setStatus("正常");
        wechatMapper.insert(wechat);

        String[][] i18n = {
                {"zh-CN", "site.name", "示例物流"},
                {"en-US", "site.name", "Example Logistics"},
                {"zh-CN", "quote.title", "在线询价"},
                {"en-US", "quote.title", "Get a Quote"},
                {"zh-CN", "track.title", "运单查询"},
                {"en-US", "track.title", "Track Shipment"}
        };
        for (String[] item : i18n) {
            I18nItem i = new I18nItem();
            i.setTenantId(tenant.getId());
            i.setLang(item[0]);
            i.setKeyName(item[1]);
            i.setValueText(item[2]);
            i18nMapper.insert(i);
        }
    }

    private void seedBills(Tenant t1, Tenant t2) {
        String[][] bills = {
                {"2026-09", "2980", "2980", "已结清"},
                {"2026-10", "2980", "2980", "已结清"},
                {"2026-11", "2980", "2980", "待支付"}
        };
        for (int i = 0; i < bills.length; i++) {
            Bill bill = new Bill();
            bill.setTenantId(i == 2 ? t2.getId() : t1.getId());
            bill.setBillNo("ZD" + String.format("%04d", bill.getTenantId()) + "2026" + String.format("%02d", 9 + i) + String.format("%04d", i + 1));
            bill.setPeriod(bills[i][0]);
            bill.setSeatFee(new BigDecimal(bills[i][1]));
            bill.setExtraFee(BigDecimal.ZERO);
            bill.setAmount(new BigDecimal(bills[i][2]));
            bill.setPaidAmount("已结清".equals(bills[i][3]) ? new BigDecimal(bills[i][2]) : BigDecimal.ZERO);
            bill.setStatus(bills[i][3]);
            bill.setPaidAt("已结清".equals(bills[i][3]) ? LocalDateTime.now().minusMonths(2 - i) : null);
            billMapper.insert(bill);
        }
    }
}
