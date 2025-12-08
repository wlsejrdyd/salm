package kr.salm.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    @NotBlank(message = "댓글 내용을 입력해주세요")
    @Size(max = 1000, message = "댓글은 1000자 이내로 입력해주세요")
    private String content;
}
