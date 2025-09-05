package aivlecloudnative.domain;

import aivlecloudnative.infra.AbstractEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString
public class AuthorAccepted extends AbstractEvent {

    private Long id; // 승인된 Author의 PK
    private Long userId; // 승인된 User의 고유 ID
    private String eventType;
    private Long timestamp;

    public AuthorAccepted() {}

    public AuthorAccepted(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }
}

