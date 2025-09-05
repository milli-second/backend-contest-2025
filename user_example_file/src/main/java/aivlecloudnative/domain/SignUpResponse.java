package aivlecloudnative.domain;

public record SignUpResponse(
        Long userId,
        String email,
        String username
) {}
