package kr.salm.community.repository;

import kr.salm.community.entity.Post;
import kr.salm.community.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostOrderByDisplayOrderAsc(Post post);

    Optional<PostImage> findByPostAndIsThumbnailTrue(Post post);

    @Modifying
    @Query("DELETE FROM PostImage pi WHERE pi.post = :post")
    void deleteByPost(@Param("post") Post post);

    long countByPost(Post post);
}
