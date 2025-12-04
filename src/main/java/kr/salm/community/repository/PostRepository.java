package kr.salm.community.repository;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.Category;
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

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.deleted = false AND p.category = :category ORDER BY p.createdAt DESC")
    Page<Post> findByCategory(@Param("category") Category category, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.deleted = false AND p.category.slug = :slug ORDER BY p.createdAt DESC")
    Page<Post> findByCategorySlug(@Param("slug") String slug, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.deleted = false AND p.author = :author ORDER BY p.createdAt DESC")
    Page<Post> findByAuthor(@Param("author") User author, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.deleted = false AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.createdAt DESC")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.category WHERE p.deleted = false ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findPopular(Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
    void decrementCommentCount(@Param("id") Long id);

    // 카테고리별 게시글 수
    long countByCategoryAndDeletedFalse(Category category);
}
