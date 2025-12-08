package kr.salm.community.controller;

import jakarta.validation.Valid;
import kr.salm.auth.service.CustomUserDetails;
import kr.salm.community.dto.*;
import kr.salm.community.service.*;
import kr.salm.core.dto.ApiResponse;
import kr.salm.core.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoApiController {

    private final VideoService videoService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VideoResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category) {
        PageResponse<VideoResponse> videos = category != null ?
                videoService.findByCategory(category, page, size) :
                videoService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoResponse>> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails != null ? userDetails.getUser() : null;
        return ResponseEntity.ok(ApiResponse.success(videoService.getDetail(id, user)));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = likeService.toggle(id, userDetails.getUser());
        var video = videoService.findByIdWithoutView(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "liked", liked,
                "likeCount", video.getLikeCount()
        )));
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean bookmarked = bookmarkService.toggle(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(Map.of("bookmarked", bookmarked)));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> comments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(commentService.findByVideo(id)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var comment = commentService.create(id, request, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(CommentResponse.from(comment)));
    }
}
