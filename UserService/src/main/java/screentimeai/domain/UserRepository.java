package screentimeai.domain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

////<<< PoEAA / Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname); // 중복 체크용
    Optional<User> findByNickname(String nickname); // ✅ 유저 조회용
}