package screentimeai.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

import screentimeai.domain.*;

////<<< PoEAA / Repository
//@RepositoryRestResource(collectionResourceRel = "users", path = "users")
//public interface UserRepository
//    extends PagingAndSortingRepository<User, Long> {}

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname); // 중복 체크용
}