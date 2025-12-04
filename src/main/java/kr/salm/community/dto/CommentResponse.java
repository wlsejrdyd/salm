package kr.salm.community.dto;

import kr.salm.community.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long id;
    private Long postId;
    private String content;
    
    private Long authorId;
    private String authorName;
    private String authorProfileImage;
    
    private Long parentId;
    private int depth;
    private boolean deleted;
    
    private LocalDateTime createdAt;
    
    private List<CommentResponse> replies;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getDisplayName())
                .authorProfileImage(comment.getAuthor().getProfileImage())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .depth(comment.getDepth())
                .deleted(comment.isDeleted())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
