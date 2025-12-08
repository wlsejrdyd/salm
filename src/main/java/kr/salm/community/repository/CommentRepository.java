package kr.salm.community.repository;

import kr.salm.community.entity.Comment;
import kr.salm.community.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.video = :video AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByVideo(@Param("video") Video video);
}
