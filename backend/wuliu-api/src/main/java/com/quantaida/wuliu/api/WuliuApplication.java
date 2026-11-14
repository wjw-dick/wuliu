package com.quantaida.wuliu.api;

import com.quantaida.wuliu.service.system.SeedService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 一后端多端壳：9 个端共用本服务与同一套租户上下文/权限。 */
@SpringBootApplication(scanBasePackages = "com.quantaida.wuliu")
@MapperScan("com.quantaida.wuliu.domain.mapper")
@EnableScheduling
@EnableAsync
public class WuliuApplication {
    public static void main(String[] args) {
        SpringApplication.run(WuliuApplication.class, args);
    }

    @Bean
    public ApplicationRunner seedRunner(SeedService seedService,
                                        @org.springframework.beans.factory.annotation.Value("${app.seed.enabled:true}") boolean enabled) {
        return args -> {
            if (enabled) {
                seedService.seed();
            }
        };
    }
}
