package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.dto.VideoResponse;
import kr.salm.community.dto.VideoUploadRequest;
import kr.salm.community.entity.*;
import kr.salm.community.repository.*;
import kr.salm.core.dto.PageResponse;
import kr.salm.core.exception.BusinessException;
import kr.salm.core.util.HtmlSanitizer;
import kr.salm.file.service.VideoFileService;
import kr.salm.file.service.VideoFileService.VideoUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final CategoryRepository categoryRepository;
    private final VideoLikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final VideoFileService fileService;

    @Transactional
    public Video upload(VideoUploadRequest request, MultipartFile videoFile, User author) {
        Category category = categoryRepository.findBySlug(request.getCategory())
                .orElseThrow(() -> BusinessException.notFound("카테고리"));

        VideoUploadResult result = fileService.upload(videoFile);

        Video video = Video.builder()
                .title(HtmlSanitizer.sanitize(request.getTitle()))
                .description(request.getDescription() != null ? HtmlSanitizer.sanitizeContent(request.getDescription()) : null)
                .author(author)
                .category(category)
                .videoPath(result.getVideoPath())
                .thumbnailPath(result.getThumbnailPath())
                .duration(result.getDuration())
                .width(result.getWidth())
                .height(result.getHeight())
                .fileSize(result.getFileSize())
                .hashtags(request.getHashtags())
                .productUrl(request.getProductUrl())
                .build();

        Video saved = videoRepository.save(video);
        log.info("영상 업로드: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Transactional
    public Video findById(Long id) {
        Video video = videoRepository.findActiveById(id)
                .orElseThrow(() -> BusinessException.notFound("영상"));
        videoRepository.incrementViewCount(id);
        return video;
    }

    @Transactional(readOnly = true)
    public Video findByIdWithoutView(Long id) {
        return videoRepository.findActiveById(id)
                .orElseThrow(() -> BusinessException.notFound("영상"));
    }

    @Transactional
    public VideoResponse getDetail(Long id, User currentUser) {
        Video video = findById(id);
        VideoResponse response = VideoResponse.from(video);

        if (currentUser != null) {
            response.setLiked(likeRepository.existsByVideoAndUser(video, currentUser));
            response.setBookmarked(bookmarkRepository.existsByVideoAndUser(video, currentUser));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> findAll(int page, int size) {
        Page<Video> videos = videoRepository.findAllActive(PageRequest.of(page, size));
        return PageResponse.of(videos, toResponseList(videos.getContent()));
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> findByCategory(String slug, int page, int size) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
        Page<Video> videos = videoRepository.findByCategory(category, PageRequest.of(page, size));
        return PageResponse.of(videos, toResponseList(videos.getContent()));
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> search(String keyword, int page, int size) {
        Page<Video> videos = videoRepository.search(keyword, PageRequest.of(page, size));
        return PageResponse.of(videos, toResponseList(videos.getContent()));
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> findLatest(int count) {
        Page<Video> videos = videoRepository.findAllActive(PageRequest.of(0, count));
        return toResponseList(videos.getContent());
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> findPopular(int count) {
        List<Video> videos = videoRepository.findPopular(PageRequest.of(0, count));
        return toResponseList(videos);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Video video = findByIdWithoutView(id);
        if (!video.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw BusinessException.forbidden("삭제 권한이 없습니다.");
        }
        video.softDelete();
        log.info("영상 삭제: id={}", id);
    }

    private List<VideoResponse> toResponseList(List<Video> videos) {
        return videos.stream().map(VideoResponse::from).collect(Collectors.toList());
    }
}
