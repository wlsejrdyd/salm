package kr.salm.community.repository;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.Video;
import kr.salm.community.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByVideoAndUser(Video video, User user);
    boolean existsByVideoAndUser(Video video, User user);
    void deleteByVideoAndUser(Video video, User user);
}
