package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.dto.PostCreateRequest;
import kr.salm.community.dto.PostResponse;
import kr.salm.community.entity.Category;
import kr.salm.community.entity.Post;
import kr.salm.community.entity.PostImage;
import kr.salm.community.repository.*;
import kr.salm.core.dto.PageResponse;
import kr.salm.core.exception.BusinessException;
import kr.salm.core.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 게시글 생성
     */
    @Transactional
    public Post create(PostCreateRequest request, User author, List<String> imagePaths) {
        // 카테고리 조회
        Category category = categoryRepository.findBySlug(request.getCategory())
                .or(() -> categoryRepository.findByName(request.getCategory()))
                .orElseThrow(() -> BusinessException.notFound("카테고리"));

        // XSS 방지
        String safeTitle = HtmlSanitizer.sanitize(request.getTitle());
        String safeContent = HtmlSanitizer.sanitizeContent(request.getContent());

        Post post = Post.builder()
                .title(safeTitle)
                .content(safeContent)
                .author(author)
                .category(category)
                .productUrl(request.getProductUrl())
                .build();

        Post saved = postRepository.save(post);

        // 이미지 저장
        if (imagePaths != null && !imagePaths.isEmpty()) {
            int thumbnailIdx = (request.getThumbnailIndex() != null &&
                    request.getThumbnailIndex() >= 0 &&
                    request.getThumbnailIndex() < imagePaths.size())
                    ? request.getThumbnailIndex() : 0;

            for (int i = 0; i < imagePaths.size(); i++) {
                PostImage image = PostImage.builder()
                        .post(saved)
                        .path(imagePaths.get(i))
                        .displayOrder(i)
                        .isThumbnail(i == thumbnailIdx)
                        .build();
                postImageRepository.save(image);

                if (i == thumbnailIdx) {
                    saved.setThumbnail(imagePaths.get(i));
                }
            }
        }

        postRepository.save(saved);
        log.info("게시글 작성: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 게시글 조회 (조회수 증가)
     */
    @Transactional
    public Post findById(Long id) {
        Post post = postRepository.findActiveById(id)
                .orElseThrow(() -> BusinessException.notFound("게시글", id));
        postRepository.incrementViewCount(id);
        return post;
    }

    /**
     * 게시글 조회 (조회수 미증가)
     */
    @Transactional(readOnly = true)
    public Post findByIdWithoutView(Long id) {
        return postRepository.findActiveById(id)
                .orElseThrow(() -> BusinessException.notFound("게시글", id));
    }

    /**
     * 게시글 상세 + 좋아요/북마크 상태 (API용)
     */
    @Transactional
    public PostResponse getDetail(Long id, User currentUser) {
        Post post = findById(id);
        List<PostImage> images = postImageRepository.findByPostOrderByDisplayOrderAsc(post);
        PostResponse response = PostResponse.from(post, images);

        if (currentUser != null) {
            response.setLiked(likeRepository.existsByPostAndUser(post, currentUser));
            response.setBookmarked(bookmarkRepository.existsByPostAndUser(post, currentUser));
        }

        return response;
    }

    /**
     * 전체 목록 (페이징)
     */
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> findAll(int page, int size) {
        Page<Post> posts = postRepository.findAllActive(PageRequest.of(page, size));
        return PageResponse.of(posts, toResponseList(posts.getContent()));
    }

    /**
     * 카테고리별 목록 (slug 또는 name)
     */
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> findByCategory(String categorySlugOrName, int page, int size) {
        Page<Post> posts = postRepository.findByCategorySlug(categorySlugOrName, PageRequest.of(page, size));
        
        // slug로 못 찾으면 name으로 시도
        if (posts.isEmpty()) {
            Category category = categoryRepository.findByName(categorySlugOrName).orElse(null);
            if (category != null) {
                posts = postRepository.findByCategory(category, PageRequest.of(page, size));
            }
        }
        
        return PageResponse.of(posts, toResponseList(posts.getContent()));
    }

    /**
     * 검색
     */
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> search(String keyword, int page, int size) {
        Page<Post> posts = postRepository.search(keyword, PageRequest.of(page, size));
        return PageResponse.of(posts, toResponseList(posts.getContent()));
    }

    /**
     * 최신 게시글
     */
    @Transactional(readOnly = true)
    public List<PostResponse> findLatest(int count) {
        Page<Post> posts = postRepository.findAllActive(PageRequest.of(0, count));
        return toResponseList(posts.getContent());
    }

    /**
     * 인기 게시글
     */
    @Transactional(readOnly = true)
    public List<PostResponse> findPopular(int count) {
        List<Post> posts = postRepository.findPopular(PageRequest.of(0, count));
        return toResponseList(posts);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public Post update(Long id, PostCreateRequest request, User currentUser) {
        Post post = findByIdWithoutView(id);

        if (!post.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw BusinessException.forbidden("수정 권한이 없습니다.");
        }

        // 카테고리 조회
        Category category = categoryRepository.findBySlug(request.getCategory())
                .or(() -> categoryRepository.findByName(request.getCategory()))
                .orElseThrow(() -> BusinessException.notFound("카테고리"));

        post.setTitle(HtmlSanitizer.sanitize(request.getTitle()));
        post.setContent(HtmlSanitizer.sanitizeContent(request.getContent()));
        post.setCategory(category);
        post.setProductUrl(request.getProductUrl());

        return postRepository.save(post);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        Post post = findByIdWithoutView(id);

        if (!post.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw BusinessException.forbidden("삭제 권한이 없습니다.");
        }

        post.softDelete();
        postRepository.save(post);
        log.info("게시글 삭제: id={}", id);
    }

    /**
     * 모든 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAllEnabled();
    }

    private List<PostResponse> toResponseList(List<Post> posts) {
        return posts.stream()
                .map(PostResponse::forList)
                .collect(Collectors.toList());
    }
}
