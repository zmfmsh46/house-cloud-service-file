package com.house.cloud.file_service.jpa;

import com.house.cloud.file_service.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long>, FileRepositoryCustom {
    @Query("SELECT count(f) > 0 FROM FileEntity f " +
            "WHERE f.userId = :userId " +
            "AND f.parentFolderId = :parentId " +
            "AND f.fileName = :fileName " +
            "AND f.isDeleted = :isDeleted")
    boolean isFolderNameDuplicate(@Param("userId") String userId,
                                  @Param("parentId") Long parentId,
                                  @Param("fileName") String fileName,
                                  @Param("isDeleted") boolean isDeleted);

    Optional<FileEntity> findByIdAndUserId(Long fileId, String userId);

    @Query("SELECT f FROM FileEntity f " +
            "WHERE f.id IN :fileIds " +
            "AND f.userId = :userId " +
            "AND f.isDeleted = false")
    List<FileEntity> findAllActiveFiles(
            @Param("userId") String userId,
            @Param("fileIds") List<Long> fileIds
    );

    List<FileEntity> findAllByUserIdAndIsDeletedTrue(String userId);

}
