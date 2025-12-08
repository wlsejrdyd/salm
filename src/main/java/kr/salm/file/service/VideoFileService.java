package kr.salm.file.service;

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

@Slf4j
@Service
public class VideoFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "avi", "webm", "mkv");
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB (인코딩 전)
    private static final int MAX_DURATION = 180;

    // 비동기 인코딩용 (서버 부하 분산)
    private final ExecutorService encodingExecutor = Executors.newFixedThreadPool(2);

    public VideoUploadResult upload(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();
        
        // 원본 저장 경로
        String tempFilename = uuid + "_temp." + ext;
        Path tempPath = Paths.get(uploadDir, "videos", datePath, tempFilename);
        
        // 최종 파일 경로 (항상 mp4)
        String finalFilename = uuid + ".mp4";
        Path finalPath = Paths.get(uploadDir, "videos", datePath, finalFilename);
        
        // 썸네일 경로
        String thumbFilename = uuid + "_thumb.jpg";
        Path thumbPath = Paths.get(uploadDir, "thumbnails", datePath, thumbFilename);

        try {
            Files.createDirectories(tempPath.getParent());
            Files.createDirectories(thumbPath.getParent());
            
            // 원본 저장
            file.transferTo(tempPath.toFile());
            log.info("원본 저장 완료: {}", tempPath);

            // 메타데이터 추출
            VideoMetadata metadata = extractMetadata(tempPath);
            
            if (metadata.duration > MAX_DURATION) {
                Files.deleteIfExists(tempPath);
                throw BusinessException.badRequest("영상 길이는 " + MAX_DURATION + "초 이하만 가능합니다.");
            }

            // 웹 최적화 인코딩
            encodeForWeb(tempPath, finalPath, metadata);
            
            // 원본 삭제
            Files.deleteIfExists(tempPath);

            // 썸네일 생성
            generateThumbnail(finalPath, thumbPath);

            // 인코딩 후 메타데이터 다시 추출
            VideoMetadata finalMetadata = extractMetadata(finalPath);

            String videoPath = "/videos/" + datePath + "/" + finalFilename;
            String thumbnailPath = "/thumbnails/" + datePath + "/" + thumbFilename;

            log.info("영상 처리 완료: {} ({}x{}, {}초)", videoPath, finalMetadata.width, finalMetadata.height, finalMetadata.duration);

            return new VideoUploadResult(videoPath, thumbnailPath, finalMetadata);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("영상 업로드 실패: {}", e.getMessage());
            // 정리
            try {
                Files.deleteIfExists(tempPath);
                Files.deleteIfExists(finalPath);
            } catch (IOException ignored) {}
            throw BusinessException.badRequest("영상 처리에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 웹 최적화 인코딩
     * - 해상도: 최대 1080p (세로 영상은 1080x1920)
     * - 비트레이트: 2~4 Mbps
     * - faststart: 스트리밍 즉시 시작
     */
    private void encodeForWeb(Path input, Path output, VideoMetadata meta) throws Exception {
        // 해상도 계산 (최대 1080p 유지, 비율 유지)
        int targetWidth = meta.width;
        int targetHeight = meta.height;
        
        int maxDimension = 1080;
        if (meta.width > meta.height) {
            // 가로 영상
            if (meta.width > maxDimension) {
                targetWidth = maxDimension;
                targetHeight = (int) Math.round((double) meta.height * maxDimension / meta.width);
                targetHeight = targetHeight / 2 * 2; // 짝수로
            }
        } else {
            // 세로 영상
            if (meta.height > 1920) {
                targetHeight = 1920;
                targetWidth = (int) Math.round((double) meta.width * 1920 / meta.height);
                targetWidth = targetWidth / 2 * 2;
            }
        }

        List<String> command = new ArrayList<>(List.of(
            "ffmpeg", "-y", "-i", input.toString(),
            "-c:v", "libx264",
            "-preset", "fast",
            "-crf", "23",
            "-profile:v", "high",
            "-level", "4.1",
            "-vf", "scale=" + targetWidth + ":" + targetHeight,
            "-c:a", "aac",
            "-b:a", "128k",
            "-movflags", "+faststart",
            "-threads", "2",
            output.toString()
        ));

        log.info("인코딩 시작: {}x{} -> {}x{}", meta.width, meta.height, targetWidth, targetHeight);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 로그 출력 (디버깅용)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("frame=") || line.contains("error")) {
                    log.debug("ffmpeg: {}", line);
                }
            }
        }

        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("인코딩 타임아웃");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("인코딩 실패: exit code " + process.exitValue());
        }

        log.info("인코딩 완료: {}", output);
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
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor(30, TimeUnit.SECONDS);

            int width = 0, height = 0, duration = 0;
            long fileSize = Files.size(filePath);

            // 간단한 파싱
            if (output.contains("\"width\"")) {
                width = extractInt(output, "\"width\":");
            }
            if (output.contains("\"height\"")) {
                height = extractInt(output, "\"height\":");
            }
            if (output.contains("\"duration\"")) {
                duration = (int) extractDouble(output, "\"duration\":");
            }

            return new VideoMetadata(width, height, duration, fileSize);
        } catch (Exception e) {
            log.warn("메타데이터 추출 실패: {}", e.getMessage());
            return new VideoMetadata(0, 0, 0, 0);
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
            Process process = pb.start();
            process.getInputStream().readAllBytes();
            process.waitFor(30, TimeUnit.SECONDS);
            log.info("썸네일 생성: {}", thumbPath);
        } catch (Exception e) {
            log.warn("썸네일 생성 실패: {}", e.getMessage());
        }
    }

    private int extractInt(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return 0;
        int start = idx + key.length();
        while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Integer.parseInt(json.substring(start, end));
    }

    private double extractDouble(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return 0;
        int start = idx + key.length();
        while (start < json.length() && !Character.isDigit(json.charAt(start)) && json.charAt(start) != '.') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        return Double.parseDouble(json.substring(start, end));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public record VideoMetadata(int width, int height, int duration, long fileSize) {}
    public record VideoUploadResult(String videoPath, String thumbnailPath, VideoMetadata metadata) {}
}
