package com.jtj.example.springcloudgateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Request relation config, such as header name etc.
 *
 * Created by jiang (jiang.taojie@foxmail.com)
 * 2018/9/27 23:15 End.
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties("jwt")
public class JwtProperties {

    private String headerName = "Authorization";
    private String headerPrefix = "Bearer ";
    private String secret = "csvraecdsavrvfcsabgfbescfavhnytjuyaw2tsrvg3qv";
    private Duration timeout = Duration.ofMinutes(10);
    private boolean maxTimeoutEnabled = true;
    private Duration maxTimeout = Duration.ofDays(10);
    private String attributeName;

}
