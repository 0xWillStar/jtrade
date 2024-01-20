package com.crypto.jtrade.front.provider.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.crypto.jtrade.front.provider.cache.RedisService;
import com.crypto.jtrade.front.provider.interceptor.AuthorityInterceptor;

/**
 * spring mvc config
 *
 * @author 0xWill
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RedisService redisService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthorityInterceptor authorityInterceptor = new AuthorityInterceptor();
        authorityInterceptor.setRedisService(redisService);
        registry.addInterceptor(authorityInterceptor).addPathPatterns("/v1/trade/**")
            .excludePathPatterns("/v1/trade/login");
    }

}
