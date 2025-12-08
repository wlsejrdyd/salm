package kr.salm.closet.entity;

import jakarta.persistence.*;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "cloth_categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    // 대분류: tops, outer, bottoms, shoes, accessories
    @Column(name = "parent_type", length = 20)
    private String parentType;

    @Column(length = 100)
    private String icon;

    // 이 카테고리 옷의 적정 온도 범위 (내장!)
    @Column(name = "temp_min")
    private Integer tempMin;

    @Column(name = "temp_max")
    private Integer tempMax;

    @Column(name = "layer_order")
    @Builder.Default
    private Integer layerOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
