package kr.salm.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadRequest {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 2000, message = "설명은 2000자 이내로 입력해주세요")
    private String description;

    @NotBlank(message = "카테고리를 선택해주세요")
    private String category;

    private String hashtags;
    private String productUrl;
}
