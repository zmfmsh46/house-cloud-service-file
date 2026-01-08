package com.house.cloud.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileChunkRequest {
    // 1. 전체 업로드 프로세스를 식별하는 ID (UUID 등을 사용)
    private String uploadId;

    // 2. 파일의 전체 이름 (확장자 포함)
    private String fileName;

    // 3. 현재 전송 중인 청크의 인덱스 (0부터 시작하거나 1부터 시작)
    private int chunkIndex;

    // 4. 전체 파일이 몇 개의 청크로 나누어졌는지에 대한 총 개수
    private int totalChunks;

    // 5. (선택사항) 폴더 업로드 시 구조 유지를 위한 상대 경로
    private String relativePath;

    // 6. (선택사항) 파일의 전체 크기 (검증용)
    private long totalSize;


    //파일이름, 논리경로, parentFolderId, 현재 청크 인덱스, 총 청크 인덱스 수, 파일크기, 파일타입
}
