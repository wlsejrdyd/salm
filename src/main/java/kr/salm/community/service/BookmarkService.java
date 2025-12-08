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
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public boolean toggle(Long videoId, User user) {
        Video video = videoRepository.findActiveById(videoId)
                .orElseThrow(() -> BusinessException.notFound("영상"));

        return bookmarkRepository.findByVideoAndUser(video, user)
                .map(bookmark -> {
                    bookmarkRepository.delete(bookmark);
                    return false;
                })
                .orElseGet(() -> {
                    bookmarkRepository.save(Bookmark.builder().video(video).user(user).build());
                    return true;
                });
    }
}
