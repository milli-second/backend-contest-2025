package screentimeai.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class RawDataId implements Serializable {
    private UUID subjectId;
    private Long timestamp;

    // equals, hashCode 구현 필요
}