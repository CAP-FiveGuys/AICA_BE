package com.aica.aivoca.global.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 생성자 주입을 통해 JwtTokenProvider 주입
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token == null) {
                throw new IllegalArgumentException("Authorization 헤더가 없습니다.");
            }

            if (!jwtTokenProvider.validateToken(token)) {
                throw new IllegalArgumentException("리프레시토큰이 일차하지 않습니다.");
            }

            Claims claims = jwtTokenProvider.getClaims(token);
            Long userId = Long.parseLong(claims.getSubject());
            String userUid = (String) claims.get("user_uid");

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(userId, userUid),
                    null,
                    List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT); // 409(401이 조금 더 적절하지만 로그아웃서비스와의 응답 코드를 맞추기위해 409로함.)
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 409, \"message\": \"" + e.getMessage() + "\"}");
        }
    }


    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
