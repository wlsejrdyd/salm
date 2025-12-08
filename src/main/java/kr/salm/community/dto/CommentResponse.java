package kr.salm.community.dto;

import kr.salm.community.entity.Comment;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorProfileImage;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorId(c.getAuthor().getId())
                .authorName(c.getAuthor().getDisplayName())
                .authorProfileImage(c.getAuthor().getProfileImage())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
