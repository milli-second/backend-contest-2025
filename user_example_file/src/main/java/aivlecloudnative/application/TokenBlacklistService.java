package aivlecloudnative.application;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redis;

    public TokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void blacklist(String token, long millisToExpire) {
        redis.opsForValue().set(token, "logout", Duration.ofMillis(millisToExpire));
    }

    public boolean isBlacklisted(String token) {
        return redis.hasKey(token);
    }
}
