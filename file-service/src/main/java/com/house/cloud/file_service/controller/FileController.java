package com.house.cloud.file_service.controller;

import com.house.cloud.file_service.domain.FileEntity;
import com.house.cloud.file_service.dto.CreateFolderRequest;
import com.house.cloud.file_service.dto.FileChunkRequest;
import com.house.cloud.file_service.dto.RenameFileNameRequest;
import com.house.cloud.file_service.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    //폴더 생성
    @PostMapping("/folder")
    public ResponseEntity<String> createFolder(@AuthenticationPrincipal String userId, @RequestBody CreateFolderRequest request) {

        FileEntity createfolder = fileService.createFolder(
                userId,
                request.getFileName(),
                request.getLogicalPath(),
                request.getParentFolderId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(createfolder.getFileName());
    }
    //이름 변경
    @PatchMapping("/rename")
    public ResponseEntity<?> renameItem(@AuthenticationPrincipal String userId, @RequestBody RenameFileNameRequest request) {

        fileService.renameFileName(
                userId,
                request.getFileId(),
                request.getNewFileName()
        );

        return ResponseEntity.ok().build();
    }
    //파일 휴지통 (여러개도)
    @PatchMapping("/move/trash-can")
    public ResponseEntity<?> moveItemToTrashCan(@AuthenticationPrincipal String userId, @RequestBody List<Long> fileIds) {

        fileService.moveItemsToTrash(userId, fileIds);

        return ResponseEntity.ok().build();
    }
    //휴지통에서 복구
    //파일 삭제 (여러개도)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteItem(@AuthenticationPrincipal String userId) {

        fileService.deleteFiles(userId);

        return ResponseEntity.noContent().build();
    }
    //파일 업로드 (여러개도)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal String userId,
                                        @RequestPart("file") MultipartFile chunk,
                                        @RequestPart("info") FileChunkRequest request) {

        fileService.uploadFiles(userId, chunk, request);

    }
    //파일 업로드 진행도 (여러개도)
    //파일 다운로드 (여러개도)
    @PostMapping("/download/{fileUuid}")
    public ResponseEntity<?> downloadFile() {

    }
    //특정 폴더 내 파일 리스트 조회
    @GetMapping("/list/{parentFolderId}")
    public ResponseEntity<Page<FileResponse>> getFileList() {

    }
    //파일 이름으로 조회 (검색)
    //파일이나 폴더를 다른 폴더로 이동
    @PatchMapping("/move")
    public ResponseEntity<?> moveFile(@RequestBody MoveRequest request) {

    }

    //사용자별 사용량 조회 (사용자가 사용중인 Byte 합산하여 반환)
    @GetMapping("/usage/{userId}")
    public ResponseEntity<Long> getUserStorageUsage(@PathVariable String userId) {

    }

}
