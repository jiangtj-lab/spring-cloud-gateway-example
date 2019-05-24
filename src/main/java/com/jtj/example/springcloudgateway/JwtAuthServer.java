package com.jtj.example.springcloudgateway;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Created by MrTT (jiang.taojie@foxmail.com)
 * 2018/9/26.
 */
@Slf4j
@Service
public class JwtAuthServer {

    @Resource
    private JwtProperties properties;
    @Resource
    private Environment environment;

    /**
     * parse token, prefix + encodedStr
     *
     * such as 'Bearer aaaa.bbbbbbbb.ccccccccccc'
     */
    public Claims parse(String token) {
        String prefix = properties.getHeaderPrefix();

        if (StringUtils.isEmpty(token)) {
            log.warn("Token is empty!");
            throw new UnsupportedJwtException("Token is empty!");
        }
        if (!token.startsWith(prefix)) {
            log.warn("Don't have prefix {}!", prefix);
            throw new UnsupportedJwtException("Unsupported authu jwt token!");
        }

        token = token.substring(prefix.length());
        Jws<Claims> claims = Jwts.parser()
                .setSigningKey(properties.getSecret())
                .parseClaimsJws(token);
        Claims body = claims.getBody();

        //Timeout
        if (properties.isMaxTimeoutEnabled()) {
            if (body.getIssuedAt() == null || body.getExpiration() == null) {
                log.warn("IssuedAt or Expiration is empty!");
                throw new RequiredTypeException("IssuedAt or Expiration is empty!");
            }
            Duration timeout = Duration.between(body.getIssuedAt().toInstant(), body.getExpiration().toInstant());
            if (timeout.compareTo(properties.getMaxTimeout()) > 0) {
                log.warn("Timeout is bigger than max timeout!");
                throw new ExpiredJwtException(claims.getHeader(), claims.getBody(), "Timeout is bigger than max timeout!");
            }
        }

        return body;
    }

    /**
     * generate token
     */
    public String generate(String audience, String subject) {
        Instant now = Instant.now();
        String applicationName = getApplicationName();
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret())))
                .setSubject(subject)
                .setIssuer(applicationName)
                .setIssuedAt(Date.from(now))
                .setAudience(audience)
                .setExpiration(Date.from(now.plusSeconds(properties.getTimeout().getSeconds())))
                .compact();
    }

    private String getApplicationName() {
        String applicationName = environment.getProperty("spring.application.name");
        if (applicationName != null) {
            return applicationName.toLowerCase();
        }
        return null;
    }

}
