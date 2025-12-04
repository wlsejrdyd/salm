package kr.salm.community.controller;

import jakarta.validation.Valid;
import kr.salm.auth.service.CustomUserDetails;
import kr.salm.community.dto.*;
import kr.salm.community.service.*;
import kr.salm.core.dto.ApiResponse;
import kr.salm.core.dto.PageResponse;
import kr.salm.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시글 REST API (앱용)
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final FileService fileService;

    /**
     * 게시글 목록
     */
    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        
        if (category != null && !category.isBlank()) {
            return ApiResponse.success(postService.findByCategory(category, page, size));
        }
        return ApiResponse.success(postService.findAll(page, size));
    }

    /**
     * 최신 게시글
     */
    @GetMapping("/latest")
    public ApiResponse<List<PostResponse>> latest(@RequestParam(defaultValue = "20") int count) {
        return ApiResponse.success(postService.findLatest(count));
    }

    /**
     * 인기 게시글
     */
    @GetMapping("/popular")
    public ApiResponse<List<PostResponse>> popular(@RequestParam(defaultValue = "10") int count) {
        return ApiResponse.success(postService.findPopular(count));
    }

    /**
     * 게시글 상세
     */
    @GetMapping("/{id}")
    public ApiResponse<PostResponse> detail(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.success(postService.getDetail(id, user));
    }

    /**
     * 검색
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<PostResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(postService.search(q, page, size));
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PostResponse> create(
            @Valid @RequestPart("data") PostCreateRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<String> savedImages = fileService.saveFiles(images);
        var post = postService.create(request, userDetails.getUser(), savedImages);
        return ApiResponse.success("게시글이 작성되었습니다.", PostResponse.from(post));
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PostResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        var post = postService.update(id, request, userDetails.getUser());
        return ApiResponse.success("게시글이 수정되었습니다.", PostResponse.from(post));
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.delete(id, userDetails.getUser());
        return ApiResponse.success("게시글이 삭제되었습니다.");
    }

    // ==================== 댓글 ====================

    /**
     * 댓글 목록
     */
    @GetMapping("/{postId}/comments")
    public ApiResponse<List<CommentResponse>> comments(@PathVariable Long postId) {
        return ApiResponse.success(commentService.findByPost(postId));
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        var comment = commentService.create(postId, request, userDetails.getUser());
        return ApiResponse.success("댓글이 작성되었습니다.", CommentResponse.from(comment));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.delete(commentId, userDetails.getUser());
        return ApiResponse.success("댓글이 삭제되었습니다.");
    }

    // ==================== 좋아요/북마크 ====================

    /**
     * 좋아요 토글
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> toggleLike(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = likeService.toggle(id, userDetails.getUser());
        return ApiResponse.success(liked ? "좋아요" : "좋아요 취소", liked);
    }

    /**
     * 북마크 토글
     */
    @PostMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> toggleBookmark(@PathVariable Long id,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean bookmarked = bookmarkService.toggle(id, userDetails.getUser());
        return ApiResponse.success(bookmarked ? "북마크 추가" : "북마크 취소", bookmarked);
    }
}
