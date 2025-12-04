package kr.salm.community.service;

import kr.salm.auth.entity.User;
import kr.salm.community.dto.PostResponse;
import kr.salm.community.entity.Bookmark;
import kr.salm.community.entity.Post;
import kr.salm.community.repository.BookmarkRepository;
import kr.salm.community.repository.PostRepository;
import kr.salm.core.dto.PageResponse;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;

    /**
     * 북마크 토글
     */
    @Transactional
    public boolean toggle(Long postId, User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글", postId));

        if (bookmarkRepository.existsByPostAndUser(post, user)) {
            bookmarkRepository.deleteByPostAndUser(post, user);
            log.info("북마크 취소: postId={}, userId={}", postId, user.getId());
            return false;
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .post(post)
                    .user(user)
                    .build();
            bookmarkRepository.save(bookmark);
            log.info("북마크 추가: postId={}, userId={}", postId, user.getId());
            return true;
        }
    }

    /**
     * 북마크 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> findMyBookmarks(User user, int page, int size) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByUser(user, PageRequest.of(page, size));
        
        return PageResponse.of(
                bookmarks,
                bookmarks.getContent().stream()
                        .map(b -> PostResponse.forList(b.getPost()))
                        .collect(Collectors.toList())
        );
    }

    /**
     * 북마크 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, User user) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글", postId));
        return bookmarkRepository.existsByPostAndUser(post, user);
    }
}
