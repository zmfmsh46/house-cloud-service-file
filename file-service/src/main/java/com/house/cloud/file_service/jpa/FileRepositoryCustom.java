package com.house.cloud.file_service.jpa;


import com.house.cloud.file_service.domain.FileEntity;

public interface FileRepositoryCustom {

    void updateChildrenToDelete(String userId, FileEntity folder);

}
