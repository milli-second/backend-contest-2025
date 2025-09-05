package aivlecloudnative.domain;

import java.util.List;

public record UserInfoResponse(
        Long id,
        String email,
        String username,
        boolean isKT,
        boolean isAuthor,
        boolean hasActiveSubscription,
        List<Long> contentHistory
) {}
