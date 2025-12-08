package kr.salm.community.dto;

import kr.salm.community.entity.Video;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String description;
    private String authorNickname;
    private Long authorId;
    private String authorProfileImage;
    private String categoryName;
    private String categorySlug;
    private String videoPath;
    private String thumbnailPath;
    private Integer duration;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private String hashtags;
    private String productUrl;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private boolean liked;
    private boolean bookmarked;

    public static VideoResponse from(Video v) {
        return from(v, false, false);
    }

    public static VideoResponse from(Video v, boolean liked, boolean bookmarked) {
        return VideoResponse.builder()
                .id(v.getId())
                .title(v.getTitle())
                .description(v.getDescription())
                .authorNickname(v.getAuthor().getNickname())
                .authorId(v.getAuthor().getId())
                .authorProfileImage(v.getAuthor().getProfileImage())
                .categoryName(v.getCategory().getName())
                .categorySlug(v.getCategory().getSlug())
                .videoPath(v.getVideoPath())
                .thumbnailPath(v.getThumbnailPath())
                .duration(v.getDuration())
                .width(v.getWidth())
                .height(v.getHeight())
                .fileSize(v.getFileSize())
                .hashtags(v.getHashtags())
                .productUrl(v.getProductUrl())
                .viewCount(v.getViewCount())
                .likeCount(v.getLikeCount())
                .commentCount(v.getCommentCount())
                .createdAt(v.getCreatedAt())
                .liked(liked)
                .bookmarked(bookmarked)
                .build();
    }

    public String getFormattedDuration() {
        if (duration == null || duration == 0) return "0:00";
        int m = duration / 60;
        int s = duration % 60;
        return String.format("%d:%02d", m, s);
    }

    // 해시태그 리스트로 변환
    public List<String> getHashtagList() {
        if (hashtags == null || hashtags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(hashtags.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
