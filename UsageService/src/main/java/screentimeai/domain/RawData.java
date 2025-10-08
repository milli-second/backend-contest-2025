package screentimeai.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "raw_data")
@IdClass(RawDataId.class)  // 복합키 사용
public class RawData {

    @Id
    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Id
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_info", nullable = false)
    private String sourceInfo;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_system_app", nullable = false)
    private Boolean isSystemApp;

    @Column(name = "is_updated_system_app")
    private Boolean isUpdatedSystemApp;

    @Column(name = "type", nullable = false)
    private String type;
}

