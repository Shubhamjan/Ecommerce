package com.demo.security;

import com.demo.service.TokenBlacklistService;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
//import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> PUBLIC_URLS = List.of("/api/users/register","/api/users/login"
            );

    @Autowired
    private TokenBlacklistService blacklistService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        String method = exchange.getRequest().getMethod().name();
        System.out.println("near the public url check");
        if(PUBLIC_URLS.stream().anyMatch(path::contains)){
            return chain.filter(exchange);
        }
        String authHeader =  exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(authHeader==null || !authHeader.startsWith("Bearer")){
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        System.out.println("the token is "+token);

//        if(!jwtUtil.validateToken(token)){
//            return unauthorized(exchange);
//        }
//
//        String role = jwtUtil.getRole(token);
//        if(!isAuthorized(role,path,method)){
//            return forbidden(exchange);
//        }
//        String userId = jwtUtil.getUserId(token);
//
//
//        ServerHttpRequest modifiedRequest = exchange
//                .getRequest()
//                .mutate()
//                .header("X-User-Id",userId)
//                .header("X-User_Role",role)
//                .build();
//
//        return chain.filter(exchange.mutate().request(modifiedRequest).build());
        // 🔴 BLACKLIST CHECK (ASYNC)
        return blacklistService.isBlacklisted(token)
                .flatMap(isBlacklisted -> {

                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return unauthorized(exchange);
                    }

                    // 🔐 JWT validation
                    if (!jwtUtil.validateToken(token)) {
                        return unauthorized(exchange);
                    }

                    String role = jwtUtil.getRole(token);
                    System.out.println("PATH FROM TOKEN: " + path);
                    System.out.println("ROLE: " + role);
                    System.out.println("METHOD: " + method);

                    String email = jwtUtil.getEmail(token);
                    System.out.println("The emails is "+email);
//                    if (!isAuthorized(role, path, method)) {
//                        return forbidden(exchange);
//                    }

                    String userId = jwtUtil.getUserId(token);
                    System.out.println("The user id: "+userId);
                    ServerHttpRequest modifiedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", role)
                            .header("X-User-Email",email)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private static final Map<String, Map<String,List<String>>> ROLE_API_PERMISSION = Map.of("ADMIN",Map.of("GET",List.of("/api/product"),
            "POST",List.of("/api/product"),
                "PUT",List.of("/api/product"),
                    "DELETE",List.of("/api/product")),
            "USER",Map.of("GET",List.of("/api/product","/api/users")));

    private boolean isAuthorized(String role,String path,String method){

        Map<String,List<String>> permission = ROLE_API_PERMISSION.get(role.substring(5));
        System.out.println("permission :- "+permission);
        if(permission==null){
            return false;
        }
        List<String> allowedPath = permission.get(method);
        if(allowedPath==null){
            return false;
        }

        return allowedPath.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
