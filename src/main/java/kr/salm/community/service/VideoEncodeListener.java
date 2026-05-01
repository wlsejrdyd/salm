package kr.salm.community.service;

import kr.salm.file.service.VideoFileService;
import kr.salm.file.service.VideoFileService.EncodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Future;

/**
 * VideoService.upload(...) 가 커밋된 뒤에야 인코딩을 큐잉.
 * 트랜잭션 롤백 시 ffmpeg 작업이 시작되지 않도록 분리됨.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoEncodeListener {

    private final VideoFileService videoFileService;
    private final VideoService videoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEncodeRequested(VideoService.EncodeRequestedEvent event) {
        Future<EncodingResult> future = videoFileService.encodeAsync(event.prep());

        Thread watcher = new Thread(() -> {
            try {
                EncodingResult r = future.get();
                if (r.success()) {
                    videoService.markReady(event.videoId());
                } else {
                    videoService.markFailed(event.videoId(), r.error());
                }
            } catch (Exception e) {
                log.error("encoding watcher 실패 (videoId={}): {}", event.videoId(), e.getMessage(), e);
                videoService.markFailed(event.videoId(), e.getMessage());
            }
        }, "video-encode-watcher-" + event.videoId());
        watcher.setDaemon(true);
        watcher.start();
    }
}
