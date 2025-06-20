package chasemoon.top.wxflearningresourcesbackendclient.service;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceFileService {
    String uploadFile(MultipartFile file, ResourceFile meta) throws Exception;
    ResourceFile getFileDetail(String fileId);
    Page<ResourceFile> listFiles(Long courseId, String keyword, Pageable pageable);
    byte[] downloadFile(String fileId) throws Exception;
    String getPreviewUrl(String fileId) throws Exception;
} 