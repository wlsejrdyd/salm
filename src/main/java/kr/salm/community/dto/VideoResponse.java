package kr.salm.community.dto;

import kr.salm.community.entity.Video;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String description;
    
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    
    private Long authorId;
    private String authorName;
    private String authorProfileImage;
    
    private String videoPath;
    private String thumbnailPath;
    private Integer duration;
    private String formattedDuration;
    
    private String hashtags;
    private List<String> hashtagList;
    private String productUrl;
    
    private int viewCount;
    private int likeCount;
    private int commentCount;
    
    private LocalDateTime createdAt;
    
    private Boolean liked;
    private Boolean bookmarked;

    public static VideoResponse from(Video v) {
        return VideoResponse.builder()
                .id(v.getId())
                .title(v.getTitle())
                .description(v.getDescription())
                .categoryId(v.getCategory().getId())
                .categoryName(v.getCategoryName())
                .categorySlug(v.getCategorySlug())
                .authorId(v.getAuthor().getId())
                .authorName(v.getAuthor().getDisplayName())
                .authorProfileImage(v.getAuthor().getProfileImage())
                .videoPath(v.getVideoPath())
                .thumbnailPath(v.getThumbnailPath())
                .duration(v.getDuration())
                .formattedDuration(v.getFormattedDuration())
                .hashtags(v.getHashtags())
                .hashtagList(v.getHashtags() != null ? List.of(v.getHashtags().split("\\s+")) : List.of())
                .productUrl(v.getProductUrl())
                .viewCount(v.getViewCount())
                .likeCount(v.getLikeCount())
                .commentCount(v.getCommentCount())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
