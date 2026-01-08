package com.house.cloud.file_service.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "unique_file_per_parent",
                columnNames = {"userId", "parentFolderId", "fileName", "deletedAt"}
        )
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자
    @Column
    private String userId;

    // [논리] 사용자가 보는 파일명 및 위치
    @Column
    private String fileName;       // 보고서.pdf
    @Column(nullable = false)
    private boolean isFolder;      // 파일인지 폴더인지 구분
    @Column
    private String logicalPath;    // /documents/2024/
    @Column
    private Long parentFolderId;   // 폴더 구조 관리용 (폴더 서비스가 따로 없다면)

    // [물리] 실제 서버 저장 정보
    @Column(unique = true)
    private String storedFileName; // UUID (8282-uuid-...)
    @Column
    private String physicalPath;   // 실제 저장 루트 경로

    // [메타]
    @Column
    private Long fileSize;
    @Column
    private String contentType;

    @CreatedDate // 생성 시 자동 저장
    @Column(updatable = false) // 수정 시에는 변경되지 않도록 설정
    private LocalDateTime createdAt;

    @LastModifiedDate // 수정 시 자동 저장
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void deleteCancel() {
        this.deletedAt = null;
    }

    public void renameFileName(String newFileName) {
        this.fileName = newFileName;
    }
}
