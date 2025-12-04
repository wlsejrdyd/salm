package kr.salm.community.entity;

import jakarta.persistence.*;
import kr.salm.auth.entity.User;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_category", columnList = "category_id"),
    @Index(name = "idx_post_author", columnList = "author_id"),
    @Index(name = "idx_post_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    @Column(length = 500)
    private String thumbnail;

    // 상품 연동 (향후 확장)
    @Column(length = 1000)
    private String productUrl;

    @Column(length = 255)
    private String productName;

    @Column(length = 50)
    private String productPrice;

    // 통계 (비정규화 - 성능 목적)
    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int commentCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }

    public void softDelete() {
        this.deleted = true;
    }

    // 이미지 추가 헬퍼
    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    public void clearImages() {
        images.clear();
    }

    // 카테고리명 조회 (편의 메서드)
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public String getCategorySlug() {
        return category != null ? category.getSlug() : null;
    }
}
