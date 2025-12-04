package kr.salm.product.entity;

import jakarta.persistence.*;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

/**
 * 상품 Entity (향후 확장용)
 * - 쿠팡 파트너스, 네이버 쇼핑 등 연동 시 사용
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_source", columnList = "source")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 50)
    private String price;

    @Column(length = 1000)
    private String productUrl;

    @Column(length = 50)
    private String category;

    // 상품 소스 (coupang, naver, etc.)
    @Column(length = 20)
    private String source;

    // 외부 상품 ID
    @Column(length = 100)
    private String externalId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
