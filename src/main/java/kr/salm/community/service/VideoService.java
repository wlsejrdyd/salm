package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.dto.VideoResponse;
import kr.salm.community.dto.VideoUploadRequest;
import kr.salm.community.entity.Category;
import kr.salm.community.entity.Video;
import kr.salm.community.repository.*;
import kr.salm.core.dto.PageResponse;
import kr.salm.core.exception.BusinessException;
import kr.salm.file.service.VideoFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final VideoFileService videoFileService;

    @Transactional
    public Video upload(VideoUploadRequest request, MultipartFile videoFile, User user) {
        Category category = categoryRepository.findBySlug(request.getCategory())
                .orElseThrow(() -> BusinessException.notFound("카테고리"));

        var result = videoFileService.upload(videoFile);

        Video video = Video.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(user)
                .category(category)
                .videoPath(result.videoPath())
                .thumbnailPath(result.thumbnailPath())
                .duration(result.metadata().duration())
                .width(result.metadata().width())
                .height(result.metadata().height())
                .fileSize(result.metadata().fileSize())
                .hashtags(request.getHashtags())
                .productUrl(request.getProductUrl())
                .build();

        return videoRepository.save(video);
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
    public VideoResponse getDetail(Long id, User user) {
        Video video = findById(id);
        boolean liked = false;
        boolean bookmarked = false;

        if (user != null) {
            liked = likeRepository.existsByVideoAndUser(video, user);
            bookmarked = bookmarkRepository.existsByVideoAndUser(video, user);
        }

        return VideoResponse.from(video, liked, bookmarked);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> findAll(int page, int size) {
        Page<Video> videos = videoRepository.findAllActive(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<VideoResponse> content = videos.getContent().stream()
                .map(v -> VideoResponse.from(v, false, false))
                .collect(Collectors.toList());
        return PageResponse.of(videos, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> findByCategory(String slug, int page, int size) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
        Page<Video> videos = videoRepository.findByCategory(category, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<VideoResponse> content = videos.getContent().stream()
                .map(v -> VideoResponse.from(v, false, false))
                .collect(Collectors.toList());
        return PageResponse.of(videos, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> search(String keyword, int page, int size) {
        Page<Video> videos = videoRepository.search(keyword, PageRequest.of(page, size));
        List<VideoResponse> content = videos.getContent().stream()
                .map(v -> VideoResponse.from(v, false, false))
                .collect(Collectors.toList());
        return PageResponse.of(videos, content);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> findLatest(int limit) {
        return videoRepository.findAllActive(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .getContent().stream()
                .map(v -> VideoResponse.from(v, false, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> findPopular(int limit) {
        return videoRepository.findPopular(PageRequest.of(0, limit))
                .stream()
                .map(v -> VideoResponse.from(v, false, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id, User user) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("영상"));
        if (!video.getAuthor().getId().equals(user.getId())) {
            throw BusinessException.forbidden("삭제 권한이 없습니다.");
        }
        video.setDeleted(true);
    }
}
