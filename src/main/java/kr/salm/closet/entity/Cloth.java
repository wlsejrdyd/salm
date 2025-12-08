package kr.salm.closet.entity;

import jakarta.persistence.*;
import kr.salm.auth.entity.User;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "clothes", indexes = {
    @Index(name = "idx_cloth_user", columnList = "user_id"),
    @Index(name = "idx_cloth_category", columnList = "category_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cloth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ClothCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(length = 50)
    private String color;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "product_url", length = 1000)
    private String productUrl;

    @Column(name = "is_favorite")
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "wear_count")
    @Builder.Default
    private Integer wearCount = 0;

    @Column(name = "last_worn_at")
    private LocalDate lastWornAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public void recordWear() {
        this.wearCount++;
        this.lastWornAt = LocalDate.now();
    }
}
