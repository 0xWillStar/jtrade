package com.crypto.jtrade.core.provider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alibaba.fastjson2.JSONFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Crypto JTrade Core Application
 *
 * @author 0xWill
 */
@Slf4j
@EnableScheduling
@EnableRabbit
@SpringBootApplication(scanBasePackages = "com.crypto.jtrade")
@MapperScan("com.crypto.jtrade.core.provider.mapper")
public class Application {

    public static void main(String[] args) {
        JSONFactory.setUseJacksonAnnotation(false);
        SpringApplication.run(Application.class, args);
    }

}
