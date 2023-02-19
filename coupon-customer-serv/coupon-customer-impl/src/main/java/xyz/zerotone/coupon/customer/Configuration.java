package xyz.zerotone.coupon.customer;


import feign.Logger;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

// Configuration注解声明配置类
@org.springframework.context.annotation.Configuration
public class Configuration {

    // 注册Bean并添加负载均衡功能
    @Bean
    @LoadBalanced
    public WebClient.Builder register() {
        return WebClient.builder();
    }


    //声明 Feign 组件的日志级别
    @Bean
    Logger.Level feignLogger() {
        return Logger.Level.FULL;
    }

}