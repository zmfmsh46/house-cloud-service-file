package com.house.cloud.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFolderRequest {
    private String fileName;

    private String logicalPath;

    private Long parentFolderId;

}
