package com.cyan.stargaze.metric;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 指标平台启动类。
 * <p>
 * 字段升级为指标/维度,多数据集绑定,口径治理;
 * 核心输出 /rpc/metric/{id}/resolve(纯函数,query 调用)与组合校验。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.cyan")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cyan")
@EnableScheduling
@MapperScan("com.cyan.stargaze.metric.infra.persistence.**.mappers")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
