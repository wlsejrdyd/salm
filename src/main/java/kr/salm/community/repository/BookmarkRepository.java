package kr.salm.community.repository;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.Bookmark;
import kr.salm.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByPostAndUser(Post post, User user);

    boolean existsByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);

    // 사용자의 북마크 목록
    @Query("SELECT b FROM Bookmark b WHERE b.user = :user AND b.post.deleted = false ORDER BY b.createdAt DESC")
    Page<Bookmark> findByUser(@Param("user") User user, Pageable pageable);
}
