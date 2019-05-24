package com.jtj.example.springcloudgateway;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by MrTT (jiang.taojie@foxmail.com)
 * 2019/5/20.
 */
@Configuration
public class GenerateJwtGatewayFilterFactory extends AbstractGatewayFilterFactory<GenerateJwtGatewayFilterFactory.Config> {

    @Resource
    private JwtProperties properties;
    @Resource
    private JwtAuthServer jwtAuthServer;

    public GenerateJwtGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public String name() {
        return "GenerateJwt";
    }

    @Override
    public GatewayFilter apply(Config config) {

        String[] place = config.getPlace().split(":");

        return (exchange, chain) -> {
            return Mono
                    .defer(() -> {
                        if ("session".equals(place[0])) {
                            return exchange.getSession().map(webSession -> {
                                return webSession.getAttributes().getOrDefault(place[1], "");
                            });
                        }
                        if ("query".equals(place[0])) {
                            String first = exchange.getRequest().getQueryParams().getFirst(place[1]);
                            return Mono.justOrEmpty(first);
                        }
                        if ("form".equals(place[0])) {
                            /*return exchange.getFormData().map(formData -> {
                                String first = formData.getFirst(place[1]);
                                return Optional.ofNullable(first).orElse("");
                            });*/
                            return new DefaultServerRequest(exchange).bodyToMono(new ParameterizedTypeReference<MultiValueMap<String, String>>() {}).map(formData -> {
                                String first = formData.getFirst(place[1]);
                                return Optional.ofNullable(first).orElse("");
                            });
                        }
                        throw new RuntimeException("不支持的类型！");
                    })
                    .filter(sub -> !StringUtils.isEmpty(sub))
                    .map(sub -> jwtAuthServer.generate(config.getAudience(), config.getPrefix() + ":" + sub))
                    .map(token -> exchange.getRequest().mutate().header(properties.getHeaderName(), properties.getHeaderPrefix() + token).build())
                    .map(req -> exchange.mutate().request(req).build())
                    .then(chain.filter(exchange));
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("place", "audience", "prefix");
    }

    @Data
    static class Config {

        /**
         * 三种类型的位置.
         * - session:<param>
         * - query:<param>
         * - form:<param>
         */
        private String place = "session:user";

        /**
         * 授权对象
         * system: 系统用户
         * wechat: 微信用户
         * etc
         */
        private String audience = "system";

        /**
         * 授权主体标识
         */
        private String prefix = "id";
    }

}
