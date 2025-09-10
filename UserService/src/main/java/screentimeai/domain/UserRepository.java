package screentimeai.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

import screentimeai.domain.*;

import java.util.Optional;

////<<< PoEAA / Repository
//@RepositoryRestResource(collectionResourceRel = "users", path = "users")
//public interface UserRepository
//    extends PagingAndSortingRepository<User, Long> {}

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname); // 중복 체크용
    Optional<User> findByNickname(String nickname); // ✅ 유저 조회용

}