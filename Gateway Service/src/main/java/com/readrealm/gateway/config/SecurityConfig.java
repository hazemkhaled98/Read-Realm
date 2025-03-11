package com.readrealm.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
public class SecurityConfig {

    private record RedirectAuthenticationSuccessHandler(
            ServerRequestCache requestCache) implements ServerAuthenticationSuccessHandler {

        @Override
        public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
                                                  Authentication authentication) {
            ServerWebExchange exchange = webFilterExchange.getExchange();


            return requestCache.getRedirectUri(exchange)
                    .defaultIfEmpty(URI.create("/"))
                    .flatMap(redirectUrl -> {
                        exchange.getResponse().getHeaders().setLocation(redirectUrl);
                        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                        return exchange.getResponse().setComplete();
                    });
        }
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ServerRequestCache requestCache) {

        http.csrf(CsrfSpec::disable)
                .requestCache(requestCacheSpec -> requestCacheSpec.requestCache(requestCache))
                .authorizeExchange(
                        exchanges -> exchanges.pathMatchers("/api/v1/orders/**").authenticated()
                                .anyExchange().permitAll())
                .oauth2Login(oauth2Login -> oauth2Login
                        .authenticationSuccessHandler(new RedirectAuthenticationSuccessHandler(requestCache)))
                .logout(logout -> logout.logoutSuccessHandler(
                        (webFilterExchange, authentication) -> webFilterExchange.getExchange()
                                .getResponse().setComplete()));
        return http.build();
    }

    @Bean
    public ServerRequestCache serverRequestCache() {
        return new WebSessionServerRequestCache();
    }
}