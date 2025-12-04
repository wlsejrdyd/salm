package kr.salm.community.entity;

import jakarta.persistence.*;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;  // 표시명: "일상", "주방"

    @Column(nullable = false, unique = true, length = 50)
    private String slug;  // URL용: "daily", "kitchen"

    @Column(length = 100)
    private String description;

    @Column(length = 50)
    private String icon;  // 이모지 또는 아이콘 클래스

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
