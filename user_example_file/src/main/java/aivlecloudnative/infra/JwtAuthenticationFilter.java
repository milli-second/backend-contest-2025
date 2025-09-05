package aivlecloudnative.infra;

import aivlecloudnative.application.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final TokenBlacklistService blacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    @NotNull HttpServletResponse res,
                                    @NotNull FilterChain chain) throws IOException, ServletException {

        String bearer = req.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);

            // 🔒 블랙리스트 확인
            if (blacklist.isBlacklisted(token)) {
                log.warn("블랙리스트 토큰 접근 차단");
            } else if (jwtProvider.validate(token)) {
                Authentication auth = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }
}