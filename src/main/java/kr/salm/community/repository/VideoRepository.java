package kr.salm.community.repository;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.Category;
import kr.salm.community.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v JOIN FETCH v.category JOIN FETCH v.author WHERE v.deleted = false ORDER BY v.createdAt DESC")
    Page<Video> findAllActive(Pageable pageable);

    @Query("SELECT v FROM Video v JOIN FETCH v.category JOIN FETCH v.author WHERE v.deleted = false AND v.category = :category ORDER BY v.createdAt DESC")
    Page<Video> findByCategory(@Param("category") Category category, Pageable pageable);

    @Query("SELECT v FROM Video v JOIN FETCH v.category JOIN FETCH v.author WHERE v.id = :id AND v.deleted = false")
    Optional<Video> findActiveById(@Param("id") Long id);

    @Query("SELECT v FROM Video v JOIN FETCH v.category JOIN FETCH v.author WHERE v.deleted = false AND (v.title LIKE %:keyword% OR v.description LIKE %:keyword%) ORDER BY v.createdAt DESC")
    Page<Video> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM Video v JOIN FETCH v.category JOIN FETCH v.author WHERE v.deleted = false ORDER BY v.likeCount DESC, v.createdAt DESC")
    List<Video> findPopular(Pageable pageable);

    @Modifying
    @Query("UPDATE Video v SET v.viewCount = v.viewCount + 1 WHERE v.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
