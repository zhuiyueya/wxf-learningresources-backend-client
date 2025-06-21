package chasemoon.top.wxflearningresourcesbackendclient.service.impl;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import chasemoon.top.wxflearningresourcesbackendclient.repository.ResourceFileRepository;
import chasemoon.top.wxflearningresourcesbackendclient.service.ResourceFileService;
import chasemoon.top.wxflearningresourcesbackendclient.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service("ResourceFileServiceImpl")
@RequiredArgsConstructor
public class ResourceFileServiceImpl implements ResourceFileService {
    private final ResourceFileRepository resourceFileRepository;
    private final MinioUtil minioUtil;

    @Override
    public String uploadFile(MultipartFile file, ResourceFile meta) throws Exception {
        log.debug("开始上传文件: {}", file.getOriginalFilename());
        meta.setFileId(java.util.UUID.randomUUID().toString());
        
        // 保存原始文件名用于预览
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null && !originalFileName.trim().isEmpty()) {
            meta.setFileName(originalFileName);
        }
        
        String fileName = minioUtil.upload(file);
        meta.setFilePath(fileName);
        meta.setCreateTime(new Date());
        meta.setLastUpdateTime(new Date());
        resourceFileRepository.save(meta);
        return fileName;
    }

    @Override
    public ResourceFile getFileDetail(String fileId) {
        return resourceFileRepository.findById(fileId).orElse(null);
    }

    @Override
    public Page<ResourceFile> listFiles(Long courseId, String keyword, Pageable pageable) {
        if ((keyword != null && !keyword.isBlank()) || courseId != null) {
            return resourceFileRepository.search(courseId, keyword, pageable);
        }
        return resourceFileRepository.findAll(pageable);
    }

    @Override
    public byte[] downloadFile(String fileId) throws Exception {
        Optional<ResourceFile> fileOpt = resourceFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) throw new RuntimeException("文件不存在");
        try (var is = minioUtil.download(fileOpt.get().getFilePath())) {
            return is.readAllBytes();
        }
    }

    @Override
    public String getPreviewUrl(String fileId) throws Exception {
        Optional<ResourceFile> fileOpt = resourceFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) throw new RuntimeException("文件不存在");
        return minioUtil.getPreviewUrl(fileOpt.get().getFilePath());
    }
} 