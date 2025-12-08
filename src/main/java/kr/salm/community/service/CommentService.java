package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.dto.*;
import kr.salm.community.entity.*;
import kr.salm.community.repository.*;
import kr.salm.core.exception.BusinessException;
import kr.salm.core.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public Comment create(Long videoId, CommentRequest request, User author) {
        Video video = videoRepository.findActiveById(videoId)
                .orElseThrow(() -> BusinessException.notFound("영상"));

        Comment comment = Comment.builder()
                .video(video)
                .author(author)
                .content(HtmlSanitizer.sanitize(request.getContent()))
                .build();

        Comment saved = commentRepository.save(comment);
        video.incrementCommentCount();
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findByVideo(Long videoId) {
        Video video = videoRepository.findActiveById(videoId)
                .orElseThrow(() -> BusinessException.notFound("영상"));
        return commentRepository.findByVideo(video).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("댓글"));

        if (!comment.getAuthor().getId().equals(user.getId()) && !user.isAdmin()) {
            throw BusinessException.forbidden("삭제 권한이 없습니다.");
        }

        comment.softDelete();
        comment.getVideo().decrementCommentCount();
    }
}
