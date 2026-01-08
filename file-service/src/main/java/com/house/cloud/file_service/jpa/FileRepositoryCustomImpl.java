package com.house.cloud.file_service.jpa;

import com.house.cloud.file_service.domain.FileEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import static com.house.cloud.file_service.domain.QFileEntity.fileEntity;

public class FileRepositoryCustomImpl implements FileRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public FileRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public void updateChildrenToDelete(String userId, FileEntity folder) {

        String folderPath = folder.getLogicalPath() + "/";

        queryFactory
            .update(fileEntity)
            .set(fileEntity.isDeleted, true)
            .where(
                    fileEntity.userId.eq(userId),
                    fileEntity.isDeleted.isFalse(),
                    fileEntity.logicalPath.startsWith(folderPath) // 하위 모든 파일/폴더 매칭
            )
            .execute();
    }
}
