package kr.salm.closet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClothRequest {
    
    private String name;
    
    @NotNull(message = "카테고리를 선택해주세요")
    private Long categoryId;
    
    private String brand;
    private String color;
    private String productUrl;
}
