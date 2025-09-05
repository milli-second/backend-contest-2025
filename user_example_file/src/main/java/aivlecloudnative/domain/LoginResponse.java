package aivlecloudnative.domain;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String userName
) {}