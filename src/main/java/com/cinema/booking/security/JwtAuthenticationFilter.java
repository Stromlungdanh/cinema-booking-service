package com.cinema.booking.security;

import com.cinema.booking.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Doc header "Authorization: Bearer <token>", set SecurityContext neu token
// hop le. Khong tu tra loi/chan request o day - thieu token hay token hong
// deu de trong SecurityContext, SecurityFilterChain (permitAll/authenticated)
// se quyet dinh 401 hay khong.
//
// KHONG danh dau @Component: WebMvcTypeExcludeFilter cua @WebMvcTest van
// "thay" cac bean implement Filter du chi la web slice, keo theo phai co san
// JwtService trong context test - SecurityConfig tu new() ra filter nay thay
// vi de Spring quan ly, tranh 16 file *ControllerTest hien co (addFilters =
// false) phai khai bao them @MockBean JwtService khong lien quan gi den
// logic dang test.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtService.parseClaims(token);
                Long userId = Long.valueOf(claims.getSubject());
                String email = claims.get("email", String.class);
                UserRole role = UserRole.valueOf(claims.get("role", String.class));

                UserPrincipal principal = new UserPrincipal(userId, email, role);
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
