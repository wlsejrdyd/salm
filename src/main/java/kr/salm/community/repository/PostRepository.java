package kr.salm.community.repository;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글만 조회
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActive(Pageable pageable);

    // 카테고리별 조회
    @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.category = :category ORDER BY p.createdAt DESC")
    Page<Post> findByCategory(@Param("category") String category, Pageable pageable);

    // 작성자별 조회
    @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.author = :author ORDER BY p.createdAt DESC")
    Page<Post> findByAuthor(@Param("author") User author, Pageable pageable);

    // ID로 조회 (삭제되지 않은 것만)
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);

    // 검색
    @Query("SELECT p FROM Post p WHERE p.deleted = false AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.createdAt DESC")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    // 인기 게시글 (좋아요 순)
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findPopular(Pageable pageable);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // 댓글 수 증가
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    // 댓글 수 감소
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
    void decrementCommentCount(@Param("id") Long id);
}
