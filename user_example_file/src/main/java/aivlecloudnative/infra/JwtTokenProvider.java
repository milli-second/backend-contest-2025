package aivlecloudnative.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private final long validityMs = 60 * 60 * 1000; // 1 시간

    public String createToken(Long userId, String email, boolean isAuthor, boolean isAdmin) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("email", email)
                .claim("isAuthor", isAuthor)
                .claim("isAdmin", isAdmin)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(claims.getSubject());
        // 필요하면 roles → GrantedAuthority 변환
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    public long getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
