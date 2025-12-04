package kr.salm.community.entity;

import jakarta.persistence.*;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "post_images", indexes = {
    @Index(name = "idx_post_image_post", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isThumbnail = false;
}
