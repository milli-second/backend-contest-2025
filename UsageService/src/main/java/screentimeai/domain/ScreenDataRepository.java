package screentimeai.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import screentimeai.domain.*;

import java.util.List;

//<<< PoEAA / Repository
//@RepositoryRestResource(
//    collectionResourceRel = "screenData",
//    path = "screenData"
//)
@Repository
public interface ScreenDataRepository
    extends JpaRepository<ScreenData, Long> {
    // 중복 체크: subject_id + timestamp
    boolean existsBySubjectIdAndTimestampEpochMs(String subjectId, Long timestampEpochMs);

    List<ScreenData> findBySubjectIdAndTypeInAndTimestampEpochMsBetweenOrderByTimestampEpochMs(
            String subjectId,
            List<String> types,
            Long start,
            Long end
    );
}
