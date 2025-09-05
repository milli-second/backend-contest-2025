package screentimeai.domain;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import screentimeai.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "screenData",
    path = "screenData"
)
public interface ScreenDataRepository
    extends PagingAndSortingRepository<ScreenData, Long> {
    @Query(
        value = "select screenData " +
        "from ScreenData screenData " +
        "where(:userId is null or screenData.userId = :userId)"
    )
    ScreenData viewUsageReport(Integer userId);
}
