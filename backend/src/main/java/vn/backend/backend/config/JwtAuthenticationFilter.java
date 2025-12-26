package vn.backend.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Kiểm tra xem request có Header Authorization chứa Bearer Token không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Trích xuất Token từ chuỗi "Bearer [token]"
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt); // Bạn cần viết thêm hàm này trong JwtService

        // 3. Nếu có email và chưa được xác thực trong SecurityContext
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Ở đây bạn kiểm tra token có hợp lệ không (hết hạn chưa, đúng định dạng không)
            if (jwtService.isTokenValid(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        Collections.emptyList() // Danh sách quyền (hiện tại để trống)
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Xác thực thành công, đưa vào Context của Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}