package com.house.cloud.file_service.service;

import com.house.cloud.file_service.advice.exception.DuplicateNameException;
import com.house.cloud.file_service.advice.exception.FileNotFoundException;
import com.house.cloud.file_service.domain.FileEntity;
import com.house.cloud.file_service.dto.CreateFolderRequest;
import com.house.cloud.file_service.dto.FileChunkRequest;
import com.house.cloud.file_service.jpa.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FileServiceImpl implements FileService{

    private final FileRepository fileRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public FileServiceImpl(FileRepository fileRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.fileRepository = fileRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public FileEntity createFolder(String userId, String fileName, String logicalPath, Long parentFolderId) {

        //해당 폴더 내 폴더이름이 이미 존재할 경우
        if (fileRepository.isFolderNameDuplicate(userId, parentFolderId, fileName, false)) {
            throw new DuplicateNameException("Duplicate name in path: " + logicalPath);
        }

        FileEntity createFileEntity = FileEntity.builder()
                .userId(userId)
                .fileName(fileName)
                .isFolder(true)
                .parentFolderId(parentFolderId)
                .logicalPath(logicalPath)
                .storedFileName(null)
                .fileSize(null)
                .contentType(null)
                .physicalPath(null)
                .isDeleted(false)
                .build();

        return fileRepository.save(createFileEntity);
    }

    @Override
    public void renameFileName(String userId, Long fileId, String newFileName) {
        //해당 폴더 내 파일이름 조회
        FileEntity fileEntity = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("해당 아이디를 찾을 수 없습니다: " + userId));

        fileEntity.renameFileName(newFileName);
        fileRepository.save(fileEntity);
    }

    @Override
    public void moveItemsToTrash(String userId, List<Long> fileIds) {
        //해당 유저의 소유이고 삭제되지 않은 파일들 조회
        List<FileEntity> files = fileRepository.findAllActiveFiles(userId, fileIds);

        if (files.isEmpty()) {
            throw new FileNotFoundException("삭제할 파일을 찾을 수 없습니다.");
        }

        // 각 항목 처리
        files.forEach(f -> {
            if(f.isDeleted()) return;

            if (f.isFolder()) {
                // 폴더인 경우: 하위 모든 항목을 경로(Path) 기반으로 일괄 업데이트
                fileRepository.updateChildrenToDelete(userId, f);
            }

            //파일 또는 폴더 본인 업데이트
            f.delete();
        });
    }

    @Override
    public void deleteFiles(String userId) {

        kafkaTemplate.send("file-delete-topic", userId);

        log.info("삭제 메시지 발행 완료 userId : {}", userId);
    }

    @Override
    public void uploadFiles(String userId, MultipartFile chunk, FileChunkRequest request) {
        String uploadId = request.getUploadId();
        String chunkKey = "upload:chunks:" + uploadId + ":" + request.getChunkIndex();
        String countKey = "upload:count:" + uploadId;

        // DB나 파일 시스템에서 해당 사용자가 올리다 만 파일이 있는지 조회
        // 있다면 그 uploadId를 반환, 없다면 새로 생성해서 반환
        // 1. [멱등성 체크] 이미 해당 청크가 Redis에 등록되어 있는지 확인
        if (Boolean.TRUE.equals(redisTemplate.hasKey(chunkKey))) {
            return ResponseEntity.ok("이미 처리된 청크입니다.");
        }

        // 2. 임시 폴더에 파일 저장
        Path tempDir = Path.of(uploadDir, "temp", uploadId);
        if (!Files.exists(tempDir)) Files.createDirectories(tempDir);
        Path chunkPath = tempDir.resolve(String.valueOf(info.getChunkIndex()));
        chunk.transferTo(chunkPath);

        // 3. Redis에 청크 완료 기록 (TTL 1시간 설정으로 좀비 데이터 방지)
        redisTemplate.opsForValue().set(chunkKey, "DONE", Duration.ofHours(1));

        // 4. [진행도 업데이트] 현재까지 모인 청크 개수 증가 (Atomic한 INCR 사용)
        Long currentCount = redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, Duration.ofHours(1));

        // 진행도 계산 (전송 단계 진행도)
        int progress = (int) (((double) currentCount / info.getTotalChunks()) * 100);
        redisTemplate.opsForValue().set("upload:progress:" + uploadId, "UPLOADING:" + progress);

        // 5. 모든 청크가 다 모였는지 확인
        if (currentCount != null && currentCount == info.getTotalChunks()) {
            FileMergeMessage message = FileMergeMessage.builder()
                    .userId(userId)
                    .uploadId(uploadId)
                    .fileName(info.getFileName())
                    .totalChunks(info.getTotalChunks())
                    .relativePath(info.getRelativePath())
                    .build();

            kafkaTemplate.send("file-merge-topic", message);
            return ResponseEntity.ok("모든 조각 수신 완료, 병합 시작!");
        }

        return ResponseEntity.ok(info.getChunkIndex() + "번 조각 수신 성공");

    }
}
