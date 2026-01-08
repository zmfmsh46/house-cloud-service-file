package com.house.cloud.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenameFileNameRequest {

    private Long fileId;
    private String newFileName;
}
