package kr.salm.closet.entity;

import jakarta.persistence.*;
import kr.salm.auth.entity.User;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "outfits", indexes = {
    @Index(name = "idx_outfit_user", columnList = "user_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outfit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "temp_min")
    private Integer tempMin;

    @Column(name = "temp_max")
    private Integer tempMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_type")
    @Builder.Default
    private WeatherType weatherType = WeatherType.CLEAR;

    @Column(length = 50)
    private String occasion;

    @Column(name = "is_ai_generated")
    @Builder.Default
    private Boolean isAiGenerated = false;

    private Integer rating;

    @OneToMany(mappedBy = "outfit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OutfitCloth> outfitClothes = new ArrayList<>();

    public enum WeatherType {
        CLEAR, CLOUDY, RAINY, SNOWY, WINDY
    }

    public void addCloth(Cloth cloth, int layerOrder) {
        OutfitCloth outfitCloth = OutfitCloth.builder()
                .outfit(this)
                .cloth(cloth)
                .layerOrder(layerOrder)
                .build();
        this.outfitClothes.add(outfitCloth);
    }
}
