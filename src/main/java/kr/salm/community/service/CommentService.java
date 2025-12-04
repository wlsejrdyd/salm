package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.dto.CommentRequest;
import kr.salm.community.dto.CommentResponse;
import kr.salm.community.entity.Comment;
import kr.salm.community.entity.Post;
import kr.salm.community.repository.CommentRepository;
import kr.salm.community.repository.PostRepository;
import kr.salm.core.exception.BusinessException;
import kr.salm.core.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * 댓글 작성
     */
    @Transactional
    public Comment create(Long postId, CommentRequest request, User author) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글", postId));

        Comment parent = null;
        int depth = 0;

        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("상위 댓글", request.getParentId()));
            depth = parent.getDepth() + 1;
            
            if (depth > 1) {
                throw BusinessException.badRequest("대댓글까지만 작성 가능합니다.");
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(HtmlSanitizer.sanitize(request.getContent()))
                .parent(parent)
                .depth(depth)
                .build();

        Comment saved = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        log.info("댓글 작성: postId={}, commentId={}", postId, saved.getId());
        return saved;
    }

    /**
     * 게시글의 댓글 목록 (계층형)
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> findByPost(Long postId) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글", postId));

        List<Comment> rootComments = commentRepository.findRootCommentsByPost(post);

        return rootComments.stream()
                .map(comment -> {
                    CommentResponse response = CommentResponse.from(comment);
                    List<Comment> replies = commentRepository.findReplies(comment);
                    response.setReplies(replies.stream()
                            .map(CommentResponse::from)
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void delete(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("댓글", commentId));

        if (!comment.getAuthor().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw BusinessException.forbidden("삭제 권한이 없습니다.");
        }

        comment.softDelete();
        commentRepository.save(comment);
        postRepository.decrementCommentCount(comment.getPost().getId());

        log.info("댓글 삭제: commentId={}", commentId);
    }
}
