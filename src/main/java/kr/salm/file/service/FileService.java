package kr.salm.file.service;

import kr.salm.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;

    @Value("${file.max-file-count:10}")
    private int maxFileCount;

    @Value("${file.max-file-size:10485760}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 다중 파일 저장
     */
    public List<String> saveFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        if (files.length > maxFileCount) {
            throw new BusinessException(
                    "최대 " + maxFileCount + "개의 파일만 업로드 가능합니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<String> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String savedPath = saveFile(file);
            if (savedPath != null) {
                savedFiles.add(savedPath);
            }
        }

        return savedFiles;
    }

    /**
     * 단일 파일 저장
     */
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 크기 검사
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                    "파일 크기는 " + (maxFileSize / 1024 / 1024) + "MB를 초과할 수 없습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 확장자 검사
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();

        if (!isAllowedExtension(extension)) {
            throw new BusinessException(
                    "허용되지 않는 파일 형식입니다: " + extension,
                    HttpStatus.BAD_REQUEST
            );
        }

        // MIME 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(
                    "허용되지 않는 파일 형식입니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            // 날짜별 디렉토리 생성
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get(uploadDir, dateDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유 파일명 생성
            String newFileName = UUID.randomUUID() + "." + extension;
            Path targetPath = uploadPath.resolve(newFileName);

            // 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 상대 경로 반환 (예: 2025/01/01/uuid.jpg)
            String relativePath = dateDir + "/" + newFileName;
            log.info("파일 저장 완료: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new BusinessException("파일 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }

        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                log.info("파일 삭제 완료: {}", relativePath);
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", relativePath, e);
            return false;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedExtensions.split(",")));
        return allowed.contains(extension.toLowerCase());
    }
}
