package com.jtj.example.springcloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringCloudApplication
public class SpringCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayApplication.class, args);
    }

    @Bean
    public NonHeaderRoutePredicateFactory nonHeaderRoutePredicateFactory(){
        return new NonHeaderRoutePredicateFactory();
    }

}
