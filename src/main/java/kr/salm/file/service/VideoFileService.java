package kr.salm.file.service;

import kr.salm.core.exception.BusinessException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class VideoFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.video.max-size:104857600}")
    private long maxSize;

    @Value("${file.video.max-duration:180}")
    private int maxDuration;

    private static final Set<String> ALLOWED = Set.of("mp4", "mov", "avi", "webm", "mkv");

    public VideoUploadResult upload(MultipartFile file) {
        validate(file);

        String ext = getExtension(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + uuid + "." + ext;
        Path absolutePath = Paths.get(uploadDir, "videos", relativePath);

        try {
            Files.createDirectories(absolutePath.getParent());
            file.transferTo(absolutePath.toFile());
            log.info("동영상 저장: {}", relativePath);

            VideoMetadata meta = extractMetadata(absolutePath);
            if (meta.duration != null && meta.duration > maxDuration) {
                Files.deleteIfExists(absolutePath);
                throw BusinessException.badRequest("영상 길이는 " + maxDuration + "초 이내여야 합니다.");
            }

            String thumbPath = generateThumbnail(absolutePath, uuid, datePath);

            return VideoUploadResult.builder()
                    .videoPath("/videos/" + relativePath)
                    .thumbnailPath(thumbPath)
                    .duration(meta.duration)
                    .width(meta.width)
                    .height(meta.height)
                    .fileSize(file.getSize())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("동영상 저장 실패: {}", e.getMessage());
            throw BusinessException.badRequest("동영상 저장에 실패했습니다.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("파일이 비어있습니다.");
        }
        if (file.getSize() > maxSize) {
            throw BusinessException.badRequest("파일 크기는 " + (maxSize / 1024 / 1024) + "MB 이하여야 합니다.");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED.contains(ext.toLowerCase())) {
            throw BusinessException.badRequest("허용되지 않는 파일 형식입니다.");
        }
    }

    private VideoMetadata extractMetadata(Path path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height,duration",
                "-of", "csv=p=0", path.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();

            if (line != null && !line.isBlank()) {
                String[] parts = line.split(",");
                return new VideoMetadata(
                    parts.length > 0 ? parseInt(parts[0]) : null,
                    parts.length > 1 ? parseInt(parts[1]) : null,
                    parts.length > 2 ? parseDouble(parts[2]) : null
                );
            }
        } catch (Exception e) {
            log.warn("메타데이터 추출 실패: {}", e.getMessage());
        }
        return new VideoMetadata(null, null, null);
    }

    private String generateThumbnail(Path videoPath, String uuid, String datePath) {
        String thumbRelative = datePath + "/" + uuid + "_thumb.jpg";
        Path thumbPath = Paths.get(uploadDir, "thumbnails", thumbRelative);

        try {
            Files.createDirectories(thumbPath.getParent());
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoPath.toString(),
                "-ss", "00:00:01", "-vframes", "1",
                "-vf", "scale=480:-1", "-q:v", "2",
                thumbPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            if (Files.exists(thumbPath)) {
                return "/thumbnails/" + thumbRelative;
            }
        } catch (Exception e) {
            log.warn("썸네일 생성 실패: {}", e.getMessage());
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseDouble(String s) {
        try { return (int) Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }

    @Getter @Builder
    public static class VideoUploadResult {
        private String videoPath;
        private String thumbnailPath;
        private Integer duration;
        private Integer width;
        private Integer height;
        private Long fileSize;
    }

    @AllArgsConstructor
    private static class VideoMetadata {
        Integer width, height, duration;
    }
}
