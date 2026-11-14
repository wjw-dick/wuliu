package com.quantaida.wuliu.api.controller;

import com.quantaida.wuliu.common.crypto.SignUtil;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.service.system.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** 本地文件访问：签名校验（≤30 分钟）+ 租户前缀隔离；demo 目录返回占位图便于演示。 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final StorageService storageService;

    @GetMapping("/{*path}")
    public void file(@PathVariable String path, @RequestParam(required = false) String e,
                     @RequestParam(required = false) String s,
                     HttpServletResponse response) throws IOException {
        String key = path;
        if (key.startsWith("demo/")) {
            serveDemo(key, response);
            return;
        }
        if (e == null || s == null || !SignUtil.verify(key + ":" + e, s)) {
            throw BizException.forbidden("链接已过期或签名无效");
        }
        if (Long.parseLong(e) * 1000 < System.currentTimeMillis()) {
            throw BizException.forbidden("链接已过期，请刷新");
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try (OutputStream out = response.getOutputStream()) {
            storageService.download(key).transferTo(out);
        }
    }

    private void serveDemo(String key, HttpServletResponse response) throws IOException {
        String name = key.substring(key.lastIndexOf('/') + 1);
        response.setContentType("image/svg+xml");
        byte[] svg = ("<svg xmlns='http://www.w3.org/2000/svg' width='400' height='280'>"
                + "<rect width='100%' height='100%' fill='#eef4ff'/>"
                + "<text x='50%' y='50%' font-size='20' text-anchor='middle' fill='#1f6feb'>"
                + name + "</text></svg>").getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(svg);
    }
}
