package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.Post;
import kr.salm.community.entity.PostLike;
import kr.salm.community.repository.PostLikeRepository;
import kr.salm.community.repository.PostRepository;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;

    /**
     * 좋아요 토글
     */
    @Transactional
    public boolean toggle(Long postId, User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글", postId));

        if (likeRepository.existsByPostAndUser(post, user)) {
            // 좋아요 취소
            likeRepository.deleteByPostAndUser(post, user);
            post.decrementLikeCount();
            postRepository.save(post);
            log.info("좋아요 취소: postId={}, userId={}", postId, user.getId());
            return false;
        } else {
            // 좋아요
            PostLike like = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            post.incrementLikeCount();
            postRepository.save(post);
            log.info("좋아요: postId={}, userId={}", postId, user.getId());
            return true;
        }
    }

    /**
     * 좋아요 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글", postId));
        return likeRepository.existsByPostAndUser(post, user);
    }
}
