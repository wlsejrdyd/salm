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
    private String videoPath;          // 원본 저장 경로 (HLS dir 또는 레거시 .mp4)
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
    private String status;             // UPLOADED | PROCESSING | READY | FAILED

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
                .status(v.getStatus() == null ? Video.Status.READY.name() : v.getStatus().name())
                .build();
    }

    public String getFormattedDuration() {
        if (duration == null || duration == 0) return "0:00";
        int m = duration / 60;
        int s = duration % 60;
        return String.format("%d:%02d", m, s);
    }

    public List<String> getHashtagList() {
        if (hashtags == null || hashtags.isBlank()) return Collections.emptyList();
        return Arrays.stream(hashtags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** HLS master playlist URL — 신규 업로드만 비어있지 않음. */
    public String getHlsUrl() {
        if (videoPath == null || isLegacyMp4()) return null;
        return videoPath.endsWith("/") ? videoPath + "master.m3u8" : videoPath + "/master.m3u8";
    }

    /** 폴백 progressive MP4 URL — 신규 업로드는 720p, 레거시 업로드는 원본 그대로. */
    public String getMp4Url() {
        if (videoPath == null) return null;
        if (isLegacyMp4()) return videoPath;
        return videoPath.endsWith("/") ? videoPath + "progressive.mp4" : videoPath + "/progressive.mp4";
    }

    public boolean isReady() {
        return status == null || "READY".equals(status);
    }

    public boolean isProcessing() {
        return "PROCESSING".equals(status) || "UPLOADED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    private boolean isLegacyMp4() {
        return videoPath != null && (videoPath.endsWith(".mp4") || videoPath.endsWith(".webm"));
    }
}
