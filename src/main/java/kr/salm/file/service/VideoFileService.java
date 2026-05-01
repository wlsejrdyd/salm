package kr.salm.file.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.salm.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 영상 파일 처리.
 *
 * 파이프라인 (B-1/B-2/B-3 이후):
 *   1) 업로드 직후 동기로 원본 저장 + 메타데이터 추출 + 길이 검증.
 *      반환되는 prepareUpload 결과는 즉시 DB에 PROCESSING 상태로 저장 가능.
 *   2) encodeAsync(...) 가 ExecutorService 에 인코딩 작업을 큐잉.
 *      산출물 디렉토리:
 *          {uploadDir}/videos/yyyy/MM/dd/{uuid}/
 *              ├── master.m3u8
 *              ├── 360p/index.m3u8 + .ts
 *              ├── 720p/index.m3u8 + .ts
 *              ├── 1080p/index.m3u8 + .ts (원본 해상도 충분할 때만)
 *              └── progressive.mp4   (HLS 미지원 폴백)
 *      썸네일: {uploadDir}/thumbnails/yyyy/MM/dd/{uuid}.jpg
 *
 * URL prefix는 모두 /media/... 로 통일하여 Nginx 가 직접 서빙.
 */
@Slf4j
@Service
public class VideoFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "avi", "webm", "mkv");
    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024; // 500MB
    private static final int MAX_DURATION = 180;

    private static final ObjectMapper JSON = new ObjectMapper();

    // 인코딩 워커: CPU/디스크 부담을 고려해 2개로 제한.
    private final ExecutorService encodingExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "video-encoder");
        t.setDaemon(true);
        return t;
    });

    /**
     * 업로드 1단계 — 동기 처리:
     *   - 검증
     *   - 원본 저장 (인코딩 대상)
     *   - 메타 추출
     *   - 산출물 디렉토리 경로 결정
     */
    public PreparedUpload prepareUpload(MultipartFile file) {
        validateFile(file);

        String ext = getExtension(file.getOriginalFilename());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();

        Path outDir = Paths.get(uploadDir, "videos", datePath, uuid);
        Path tempPath = outDir.resolve("source." + ext);
        Path thumbPath = Paths.get(uploadDir, "thumbnails", datePath, uuid + ".jpg");

        try {
            Files.createDirectories(outDir);
            Files.createDirectories(thumbPath.getParent());
            file.transferTo(tempPath.toFile());
        } catch (IOException e) {
            throw BusinessException.badRequest("원본 저장에 실패했습니다: " + e.getMessage());
        }

        VideoMetadata meta = extractMetadata(tempPath);
        if (meta.duration() > MAX_DURATION) {
            try { deleteRecursively(outDir); } catch (IOException ignored) {}
            throw BusinessException.badRequest("영상 길이는 " + MAX_DURATION + "초 이하만 가능합니다.");
        }
        if (meta.width() == 0 || meta.height() == 0) {
            try { deleteRecursively(outDir); } catch (IOException ignored) {}
            throw BusinessException.badRequest("영상 메타데이터를 읽을 수 없습니다.");
        }

        // 클라이언트가 master.m3u8 / poster 를 요청할 수 있는 공개 경로
        String videoPath = "/media/videos/" + datePath + "/" + uuid;       // 디렉토리
        String thumbnailPath = "/media/thumbnails/" + datePath + "/" + uuid + ".jpg";

        return new PreparedUpload(uuid, tempPath, outDir, thumbPath, videoPath, thumbnailPath, meta);
    }

    /**
     * 업로드 2단계 — 비동기 인코딩 + 썸네일.
     * 호출자는 Future 를 통해 결과를 알 수 있고, 보통은 콜백으로 DB status 갱신.
     */
    public Future<EncodingResult> encodeAsync(PreparedUpload prep) {
        return encodingExecutor.submit(() -> {
            try {
                encodeHls(prep.tempPath(), prep.outDir(), prep.metadata());
                generateThumbnail(prep.tempPath(), prep.thumbPath());
                Files.deleteIfExists(prep.tempPath());
                log.info("HLS 인코딩 완료: {}", prep.outDir());
                return new EncodingResult(true, null);
            } catch (Exception e) {
                log.error("HLS 인코딩 실패: {}", e.getMessage(), e);
                return new EncodingResult(false, e.getMessage());
            }
        });
    }

    /**
     * ABR HLS 인코딩 — ffmpeg 한 번 호출로 360p/720p/1080p + master.m3u8 생성.
     * 동시에 progressive.mp4 (720p) 폴백도 별도로 생성.
     */
    private void encodeHls(Path source, Path outDir, VideoMetadata meta) throws Exception {
        boolean canProduce1080p = Math.max(meta.width(), meta.height()) >= 1080;
        boolean portrait = meta.height() >= meta.width();

        // 각 ladder 의 가로/세로 (세로 영상이면 스왑)
        int[][] ladders = canProduce1080p
            ? new int[][]{ res(360, portrait), res(720, portrait), res(1080, portrait) }
            : new int[][]{ res(360, portrait), res(720, portrait) };
        int[] bitratesK = canProduce1080p ? new int[]{ 800, 2500, 5000 } : new int[]{ 800, 2500 };

        // 디렉토리 미리 생성
        for (int i = 0; i < ladders.length; i++) {
            Files.createDirectories(outDir.resolve(label(i, canProduce1080p)));
        }

        List<String> cmd = new ArrayList<>();
        cmd.addAll(List.of("ffmpeg", "-y", "-i", source.toString()));

        // 각 출력에 대해 매핑 + 스케일 + 비트레이트
        StringBuilder filterComplex = new StringBuilder();
        for (int i = 0; i < ladders.length; i++) {
            int w = ladders[i][0], h = ladders[i][1];
            filterComplex.append("[0:v]scale=").append(w).append(":").append(h)
                         .append(":force_original_aspect_ratio=decrease,")
                         .append("pad=").append(w).append(":").append(h)
                         .append(":(ow-iw)/2:(oh-ih)/2,setsar=1[v").append(i).append("];");
        }
        if (filterComplex.length() > 0) {
            filterComplex.setLength(filterComplex.length() - 1); // trailing ;
        }
        cmd.addAll(List.of("-filter_complex", filterComplex.toString()));

        for (int i = 0; i < ladders.length; i++) {
            cmd.addAll(List.of(
                "-map", "[v" + i + "]",
                "-map", "0:a?",
                "-c:v:" + i, "libx264",
                "-preset:v:" + i, "veryfast",
                "-profile:v:" + i, "high",
                "-level:v:" + i, "4.1",
                "-b:v:" + i, bitratesK[i] + "k",
                "-maxrate:v:" + i, (int)(bitratesK[i] * 1.07) + "k",
                "-bufsize:v:" + i, (bitratesK[i] * 2) + "k",
                "-g", "48", "-keyint_min", "48", "-sc_threshold", "0"
            ));
        }
        cmd.addAll(List.of(
            "-c:a", "aac", "-b:a", "128k", "-ac", "2",
            "-f", "hls",
            "-hls_time", "4",
            "-hls_playlist_type", "vod",
            "-hls_segment_filename", outDir.resolve("v%v/seg_%03d.ts").toString(),
            "-master_pl_name", "master.m3u8",
            "-var_stream_map", buildVarStreamMap(ladders.length),
            outDir.resolve("v%v/index.m3u8").toString()
        ));

        runFFmpeg(cmd, "hls");

        // ffmpeg 가 v0/, v1/, v2/ 로 출력 → 우리 라벨(360p, 720p, 1080p)로 rename
        for (int i = 0; i < ladders.length; i++) {
            Path from = outDir.resolve("v" + i);
            Path to = outDir.resolve(label(i, canProduce1080p));
            if (Files.exists(from)) {
                if (Files.exists(to)) deleteRecursively(to);
                Files.move(from, to);
            }
        }
        // master.m3u8 안의 v0/index.m3u8 참조도 새 디렉토리명으로 치환
        Path master = outDir.resolve("master.m3u8");
        if (Files.exists(master)) {
            String content = Files.readString(master);
            for (int i = 0; i < ladders.length; i++) {
                content = content.replace("v" + i + "/index.m3u8",
                                          label(i, canProduce1080p) + "/index.m3u8");
            }
            Files.writeString(master, content);
        }

        // 폴백 progressive.mp4 (720p, faststart)
        int[] fb = res(720, portrait);
        List<String> mp4Cmd = List.of(
            "ffmpeg", "-y", "-i", source.toString(),
            "-vf", "scale=" + fb[0] + ":" + fb[1] + ":force_original_aspect_ratio=decrease",
            "-c:v", "libx264", "-preset", "veryfast", "-crf", "23",
            "-c:a", "aac", "-b:a", "128k",
            "-movflags", "+faststart",
            outDir.resolve("progressive.mp4").toString()
        );
        runFFmpeg(mp4Cmd, "mp4-fallback");
    }

    private String buildVarStreamMap(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(' ');
            sb.append("v:").append(i).append(",a:").append(i);
        }
        return sb.toString();
    }

    private int[] res(int target, boolean portrait) {
        // target 은 짧은 변 기준으로 해석 (세로 영상이면 너비, 가로 영상이면 높이)
        int w, h;
        if (portrait) {
            w = target;
            h = (target * 16 / 9 / 2) * 2; // 9:16 세로 → 짝수 강제
        } else {
            h = target;
            w = (target * 16 / 9 / 2) * 2;
        }
        return new int[]{ w, h };
    }

    private String label(int idx, boolean has1080) {
        if (has1080) return new String[]{ "360p", "720p", "1080p" }[idx];
        return new String[]{ "360p", "720p" }[idx];
    }

    private void runFFmpeg(List<String> cmd, String tag) throws Exception {
        log.info("ffmpeg [{}] start: {}", tag, String.join(" ", cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("error") || line.contains("Error")) {
                    log.warn("ffmpeg [{}]: {}", tag, line);
                }
            }
        }
        boolean done = process.waitFor(10, TimeUnit.MINUTES);
        if (!done) {
            process.destroyForcibly();
            throw new RuntimeException("ffmpeg [" + tag + "] 타임아웃");
        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("ffmpeg [" + tag + "] 실패: exit " + process.exitValue());
        }
    }

    private void generateThumbnail(Path videoPath, Path thumbPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", videoPath.toString(),
                "-ss", "00:00:01", "-vframes", "1",
                "-vf", "scale=480:-2",
                "-q:v", "2",
                thumbPath.toString()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.getInputStream().readAllBytes();
            p.waitFor(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("썸네일 생성 실패: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("파일이 없습니다.");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw BusinessException.badRequest("지원하지 않는 파일 형식입니다. (mp4, mov, avi, webm, mkv)");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("파일 크기는 500MB 이하만 가능합니다.");
        }
    }

    public VideoMetadata extractMetadata(Path filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "quiet", "-print_format", "json",
                "-show_format", "-show_streams", filePath.toString()
            );
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor(30, TimeUnit.SECONDS);

            JsonNode root = JSON.readTree(output);
            int width = 0, height = 0, duration = 0;
            JsonNode streams = root.path("streams");
            if (streams.isArray()) {
                for (JsonNode s : streams) {
                    if ("video".equals(s.path("codec_type").asText())) {
                        width = s.path("width").asInt(0);
                        height = s.path("height").asInt(0);
                        break;
                    }
                }
            }
            JsonNode format = root.path("format");
            if (format.has("duration")) {
                duration = (int) Math.round(format.path("duration").asDouble(0));
            }
            long fileSize = Files.size(filePath);
            return new VideoMetadata(width, height, duration, fileSize);
        } catch (Exception e) {
            log.warn("메타데이터 추출 실패: {}", e.getMessage());
            return new VideoMetadata(0, 0, 0, 0);
        }
    }

    public void deleteOutputs(String videoPath, String thumbnailPath) {
        try {
            if (videoPath != null && videoPath.startsWith("/media/videos/")) {
                Path dir = Paths.get(uploadDir, videoPath.substring("/media/".length()));
                if (Files.isDirectory(dir)) deleteRecursively(dir);
            }
            if (thumbnailPath != null && thumbnailPath.startsWith("/media/thumbnails/")) {
                Path f = Paths.get(uploadDir, thumbnailPath.substring("/media/".length()));
                Files.deleteIfExists(f);
            }
        } catch (IOException e) {
            log.warn("산출물 삭제 실패: {}", e.getMessage());
        }
    }

    private void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public record VideoMetadata(int width, int height, int duration, long fileSize) {}

    public record PreparedUpload(
        String uuid,
        Path tempPath,
        Path outDir,
        Path thumbPath,
        String videoPath,        // 공개 URL 디렉토리 (master.m3u8 의 부모)
        String thumbnailPath,    // 공개 URL
        VideoMetadata metadata
    ) {}

    public record EncodingResult(boolean success, String error) {}
}
