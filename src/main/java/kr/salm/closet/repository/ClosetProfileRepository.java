package kr.salm.closet.repository;

import kr.salm.auth.entity.User;
import kr.salm.closet.entity.ClosetProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClosetProfileRepository extends JpaRepository<ClosetProfile, Long> {
    Optional<ClosetProfile> findByUser(User user);
    boolean existsByUser(User user);
}
