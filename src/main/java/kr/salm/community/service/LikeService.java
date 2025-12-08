package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.entity.*;
import kr.salm.community.repository.*;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final VideoLikeRepository likeRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public boolean toggle(Long videoId, User user) {
        Video video = videoRepository.findActiveById(videoId)
                .orElseThrow(() -> BusinessException.notFound("영상"));

        return likeRepository.findByVideoAndUser(video, user)
                .map(like -> {
                    likeRepository.delete(like);
                    video.decrementLikeCount();
                    return false;
                })
                .orElseGet(() -> {
                    likeRepository.save(VideoLike.builder().video(video).user(user).build());
                    video.incrementLikeCount();
                    return true;
                });
    }
}
