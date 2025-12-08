package kr.salm.community.entity;

import jakarta.persistence.*;
import kr.salm.auth.entity.User;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "videos", indexes = {
    @Index(name = "idx_video_author", columnList = "author_id"),
    @Index(name = "idx_video_category", columnList = "category_id"),
    @Index(name = "idx_video_created", columnList = "created_at DESC")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 동영상 정보
    @Column(nullable = false, length = 500)
    private String videoPath;

    @Column(length = 500)
    private String thumbnailPath;

    @Column
    private Integer duration;  // 초

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column
    private Long fileSize;

    // 해시태그
    @Column(length = 500)
    private String hashtags;

    // 상품 링크
    @Column(length = 1000)
    private String productUrl;

    // 통계
    @Column(nullable = false) @Builder.Default
    private int viewCount = 0;

    @Column(nullable = false) @Builder.Default
    private int likeCount = 0;

    @Column(nullable = false) @Builder.Default
    private int commentCount = 0;

    @Column(nullable = false) @Builder.Default
    private boolean deleted = false;

    public void incrementViewCount() { this.viewCount++; }
    public void incrementLikeCount() { this.likeCount++; }
    public void decrementLikeCount() { if (this.likeCount > 0) this.likeCount--; }
    public void incrementCommentCount() { this.commentCount++; }
    public void decrementCommentCount() { if (this.commentCount > 0) this.commentCount--; }
    public void softDelete() { this.deleted = true; }

    public String getCategoryName() { return category != null ? category.getName() : null; }
    public String getCategorySlug() { return category != null ? category.getSlug() : null; }

    public String getFormattedDuration() {
        if (duration == null || duration == 0) return "0:00";
        return String.format("%d:%02d", duration / 60, duration % 60);
    }
}
