/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtj.example.springcloudgateway;

import lombok.Data;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Spencer Gibb
 */
public class NonHeaderRoutePredicateFactory extends AbstractRoutePredicateFactory<NonHeaderRoutePredicateFactory.Config> {

	public static final String HEADER_KEY = "header";
	public static final String REGEXP_KEY = "regexp";

	public NonHeaderRoutePredicateFactory() {
		super(Config.class);
	}
	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList(HEADER_KEY, REGEXP_KEY);
	}
	@Override
	public Predicate<ServerWebExchange> apply(Config config) {
		boolean hasRegex = !StringUtils.isEmpty(config.regexp);
		return exchange -> {
			List<String> values = exchange.getRequest().getHeaders().getOrDefault(config.header, Collections.emptyList());
			if (values.isEmpty()) {
				return true;
			}
			if (hasRegex) {
				return values.stream().noneMatch(value -> value.matches(config.regexp));
			}
			return false;
		};
	}
	@Data
	@Validated
	public static class Config {
		private String regexp;
		@NotEmpty
		private String header;
	}
}
