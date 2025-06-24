package com.cts.filter;

import com.cts.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class GatewayJwtValidationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GatewayJwtValidationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // UPDATED PUBLIC_URL_PATTERNS
    private static final List<String> PUBLIC_URL_PATTERNS = Arrays.asList(
            // Paths for explicit routes (if you were to use /api/auth/login directly)
            "/api/auth/login",
            "/api/auth/register",
            // Add specific prefixed paths for public endpoints when accessed via Discovery Locator
            "/authservice1/api/auth/login", // ADD THIS LINE
            "/authservice1/api/auth/register", // ADD THIS LINE
            "/api/v1/products/guest/**", // Keep existing generic path

            "/eureka/**", // Keep Eureka paths as public

            // Swagger/OpenAPI paths - ensure both prefixed and non-prefixed versions are here
            "/auth-service/v3/api-docs/**",
            "/auth-service/swagger-ui.html",
            "/auth-service/swagger-ui/**",
            "/authservice1/v3/api-docs/**", // Already correct
            "/authservice1/swagger-ui.html", // Already correct
            "/authservice1/swagger-ui/**", // Already correct

            "/user2/v3/api-docs/**",
            "/user2/swagger-ui.html",
            "/user2/swagger-ui/**",
            "/user3/v3/api-docs/**", // Already correct
            "/user3/swagger-ui.html", // Already correct
            "/user3/swagger-ui/**", // Already correct

            "/address-service/v3/api-docs/**",
            "/address-service/swagger-ui.html",
            "/address-service/swagger-ui/**",
            
            "/cartmodule/v3/api-docs/**",
            "/cartmodule/swagger-ui.html",
            "/cartmodule/swagger-ui/**",

            "/project/v3/api-docs/**",
            "/project/swagger-ui.html",
            "/project/swagger-ui/**",

            // General patterns (less specific, use if strict service ID prefixes aren't always used)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        logger.info("Incoming request path: {}", path);

        boolean isPublicUrl = PUBLIC_URL_PATTERNS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublicUrl) {
            logger.debug("Public URL: {}. Skipping JWT validation.", path);
            return chain.filter(exchange);
        }

        // --- REST OF YOUR EXISTING JWT VALIDATION LOGIC BELOW ---
        // ... (no changes needed here)
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for protected path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            if (!jwtUtil.validateToken(token)) {
                logger.warn("Invalid JWT token for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String email = jwtUtil.extractEmail(token);
            Integer userId = jwtUtil.extractUserId(token);
            List<String> roles = jwtUtil.extractRoles(token);

            logger.info("JWT validated. User: {}, ID: {}, Roles: {} for path: {}", email, userId, roles, path);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Email", email)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (SignatureException | MalformedJwtException e) {
            logger.warn("JWT Signature/Malformed error for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            logger.error("An unexpected error occurred during JWT validation for path {}: {}", path, e.getMessage(), e);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Ensure this filter runs early
    }
}