package kr.salm.closet.dto;

import kr.salm.closet.entity.Cloth;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothResponse {
    private Long id;
    private String name;
    private String categoryName;
    private String categorySlug;
    private String parentType;
    private String brand;
    private String color;
    private String imagePath;
    private String productUrl;
    private Boolean isFavorite;
    private Integer wearCount;
    private LocalDate lastWornAt;
    private LocalDateTime createdAt;

    public static ClothResponse from(Cloth c) {
        return ClothResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .categoryName(c.getCategory().getName())
                .categorySlug(c.getCategory().getSlug())
                .parentType(c.getCategory().getParentType())
                .brand(c.getBrand())
                .color(c.getColor())
                .imagePath(c.getImagePath())
                .productUrl(c.getProductUrl())
                .isFavorite(c.getIsFavorite())
                .wearCount(c.getWearCount())
                .lastWornAt(c.getLastWornAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
