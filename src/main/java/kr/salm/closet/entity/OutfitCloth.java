package kr.salm.closet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outfit_clothes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutfitCloth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    private Outfit outfit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id", nullable = false)
    private Cloth cloth;

    @Column(name = "layer_order")
    @Builder.Default
    private Integer layerOrder = 0;
}
