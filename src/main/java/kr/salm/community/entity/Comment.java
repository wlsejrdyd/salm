package kr.salm.community.entity;

import jakarta.persistence.*;
import kr.salm.auth.entity.User;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_video", columnList = "video_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false) @Builder.Default
    private boolean deleted = false;

    public void softDelete() { this.deleted = true; }
}
