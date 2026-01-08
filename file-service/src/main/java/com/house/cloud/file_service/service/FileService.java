package com.house.cloud.file_service.service;

import com.house.cloud.file_service.domain.FileEntity;
import com.house.cloud.file_service.dto.CreateFolderRequest;
import com.house.cloud.file_service.dto.FileChunkRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileEntity createFolder(String userId, String fileName, String logicalPath, Long parentFolderId);

    void renameFileName(String userId, Long fileId, String newFileName);

    void moveItemsToTrash(String userId, List<Long> fileIds);

    void deleteFiles(String userId);

    void uploadFiles(String userId, MultipartFile chunk, FileChunkRequest request);
}
