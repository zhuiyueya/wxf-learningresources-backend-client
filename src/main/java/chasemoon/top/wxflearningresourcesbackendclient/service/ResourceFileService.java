package chasemoon.top.wxflearningresourcesbackendclient.service;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ResourceFileService {
    String uploadFile(MultipartFile file, ResourceFile meta) throws Exception;
    ResourceFile getFileDetail(String fileId);
    Page<ResourceFile> listFiles(Long courseId, String keyword, Pageable pageable);
    byte[] downloadFile(String fileId) throws Exception;
    Map<String, Object> createPreview(String fileId) throws Exception;
    void approveFile(String fileId);
    void deleteFile(String fileId);
    long countAllFiles();
    List<ResourceFile> listAllFiles();
} 