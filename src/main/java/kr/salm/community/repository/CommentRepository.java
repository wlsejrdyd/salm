package kr.salm.community.repository;

import kr.salm.community.entity.Comment;
import kr.salm.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 댓글 목록 (계층형)
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPost(@Param("post") Post post);

    // 대댓글 목록
    @Query("SELECT c FROM Comment c WHERE c.parent = :parent ORDER BY c.createdAt ASC")
    List<Comment> findReplies(@Param("parent") Comment parent);

    // 게시글의 전체 댓글 수
    long countByPostAndDeletedFalse(Post post);

    // 사용자의 댓글 목록
    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorId(@Param("userId") Long userId, Pageable pageable);
}
