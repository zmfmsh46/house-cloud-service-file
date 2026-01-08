package com.house.cloud.file_service.service.kafka;

import com.house.cloud.file_service.domain.FileEntity;
import com.house.cloud.file_service.jpa.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileEventListener {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileRepository fileRepository;

    @KafkaListener(topics = "file-delete-topic", groupId = "file-service-group")
    public void processFileDelete(String userId) {
        log.info("카프카 메시지 수신: 유저 {}의 파일 삭제 시작", userId);

        // 1. 해당 유저의 휴지통 파일 목록 조회
        List<FileEntity> allFiles = fileRepository.findAllByUserIdAndIsDeletedTrue(userId);

        if (allFiles.isEmpty()) {
            return;
        }

        // 2. 물리 파일 삭제 진행
        List<FileEntity> successList = new ArrayList<>();
        for (FileEntity file : allFiles) {
            try {
                Files.deleteIfExists(Path.of(uploadDir, file.getPhysicalPath()));
                successList.add(file);
            } catch (IOException e) {
                log.error("파일 삭제 중 오류 발생: {}", Path.of(uploadDir, file.getPhysicalPath()), e);
            }
        }

        // 3. DB 최종 삭제
        if (!successList.isEmpty()) {
            fileRepository.deleteAllInBatch(successList);
        }
    }

}
