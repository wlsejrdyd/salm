package kr.salm.community.dto;

import kr.salm.community.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    
    // 작성자 정보
    private Long authorId;
    private String authorName;
    private String authorProfileImage;
    
    // 이미지
    private String thumbnail;
    private List<String> images;
    
    // 상품 정보
    private String productUrl;
    private String productName;
    private String productPrice;
    
    // 통계
    private int viewCount;
    private int likeCount;
    private int commentCount;
    
    // 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 현재 사용자 상태 (API용)
    private Boolean liked;
    private Boolean bookmarked;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getDisplayName())
                .authorProfileImage(post.getAuthor().getProfileImage())
                .thumbnail(post.getThumbnail())
                .images(post.getImages())
                .productUrl(post.getProductUrl())
                .productName(post.getProductName())
                .productPrice(post.getProductPrice())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 목록용 (컨텐츠 요약)
     */
    public static PostResponse forList(Post post) {
        PostResponse response = from(post);
        if (response.getContent() != null && response.getContent().length() > 200) {
            response.setContent(response.getContent().substring(0, 200) + "...");
        }
        return response;
    }
}
