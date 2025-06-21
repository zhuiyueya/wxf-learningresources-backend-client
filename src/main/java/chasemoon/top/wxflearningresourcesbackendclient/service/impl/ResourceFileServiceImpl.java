package chasemoon.top.wxflearningresourcesbackendclient.service.impl;

import chasemoon.top.wxflearningresourcesbackendclient.entity.Course;
import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import chasemoon.top.wxflearningresourcesbackendclient.entity.enums.DeleteFlag;
import chasemoon.top.wxflearningresourcesbackendclient.entity.enums.FileStatus;
import chasemoon.top.wxflearningresourcesbackendclient.exception.ServiceException;
import chasemoon.top.wxflearningresourcesbackendclient.repository.CourseRepository;
import chasemoon.top.wxflearningresourcesbackendclient.repository.ResourceFileRepository;
import chasemoon.top.wxflearningresourcesbackendclient.service.FileHelperService;
import chasemoon.top.wxflearningresourcesbackendclient.service.KkFileViewService;
import chasemoon.top.wxflearningresourcesbackendclient.service.ResourceFileService;
import chasemoon.top.wxflearningresourcesbackendclient.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service("ResourceFileServiceImpl")
@RequiredArgsConstructor
public class ResourceFileServiceImpl implements ResourceFileService {
    private final ResourceFileRepository resourceFileRepository;
    private final CourseRepository courseRepository;
    private final MinioUtil minioUtil;
    private final KkFileViewService kkFileViewService;
    private final FileHelperService fileHelperService;

    @Override
    public String uploadFile(MultipartFile file, ResourceFile meta) throws Exception {
        log.debug("开始上传文件: {}", file.getOriginalFilename());
        meta.setFileId(java.util.UUID.randomUUID().toString());
        
        // 检查并设置Course
        if (meta.getCourse() == null || meta.getCourse().getId() == null) {
            throw new ServiceException(400, "必须指定课程ID");
        }
        Course course = courseRepository.findById(meta.getCourse().getId())
                .orElseThrow(() -> new ServiceException(404, "指定的课程不存在"));
        meta.setCourse(course);

        // 如果前端没有传递userId，则设置一个默认值（临时方案）
        if (meta.getUserId() == null || meta.getUserId().isBlank()) {
            meta.setUserId("test-user"); // TODO: 将来从SecurityContext获取真实用户ID
        }
        
        // 保存原始文件名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null && !originalFileName.trim().isEmpty()) {
            meta.setFileName(originalFileName);
        }
        
        // 根据文件名设置文件类型和分类
        String fileTypeString = fileHelperService.getFileType(originalFileName);
        meta.setFileType(getFileTypeInt(fileTypeString));
        meta.setFileCategory(getFileCategoryInt(fileTypeString));
        meta.setFolderType((byte) 0); // 默认为文件
        
        String fileName = minioUtil.upload(file);
        meta.setFilePath(fileName);
        meta.setCreateTime(new Date());
        meta.setLastUpdateTime(new Date());
        resourceFileRepository.save(meta);
        return fileName;
    }

    private Byte getFileTypeInt(String fileType) {
        // 这里的实现可以更复杂，例如从数据库或配置中读取
        if (fileType == null) return 0; // 其他
        switch (fileType) {
            case "video": return 1;
            case "audio": return 2;
            case "image": return 3;
            case "pdf": return 4;
            case "word": return 5;
            case "powerpoint": return 6;
            case "excel": return 7;
            case "text": return 8;
            default: return 0;
        }
    }

    private Byte getFileCategoryInt(String fileType) {
        if (fileType == null) return 5; // 其他
        switch (fileType) {
            case "video": return 1;
            case "audio": return 2;
            case "image": return 3;
            case "pdf":
            case "word":
            case "powerpoint":
            case "excel":
            case "text":
                return 4; // 文档
            default:
                return 5; // 其他
        }
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
    public Map<String, Object> createPreview(String fileId) throws Exception {
        log.info("开始创建预览，fileId: {}", fileId);
        ResourceFile file = getFileDetail(fileId);
        if (file == null) {
            log.warn("文件不存在，fileId: {}", fileId);
            throw new RuntimeException("文件不存在");
        }

        log.info("找到文件: {}", file);
        String fileName = file.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("文件名为空，使用默认处理");
            fileName = "unknown";
        }

        String fileType = fileHelperService.getFileType(fileName);
        String previewType = fileHelperService.getPreviewTypeFromFileType(fileType);

        log.info("初步识别 - 文件名: {}, 文件类型: {}, 预览类型: {}", fileName, fileType, previewType);

        // 如果通过文件名无法识别，尝试通过Content-Type识别
        if ("unknown".equals(fileType)) {
            try {
                String contentType = minioUtil.getStat(file.getFilePath()).contentType();
                fileType = fileHelperService.getFileTypeFromContentType(contentType);
                previewType = fileHelperService.getPreviewTypeFromFileType(fileType);
                log.info("通过Content-Type识别文件类型: {} -> {}", contentType, fileType);
            } catch (Exception e) {
                log.warn("无法获取Content-Type: {}", e.getMessage());
            }
        }

        log.info("最终结果 - 文件名: {}, 文件类型: {}, 预览类型: {}", fileName, fileType, previewType);
        Map<String, Object> result = new HashMap<>();

        // 根据预览类型设置不同的URL
        if ("office".equals(previewType)) {
            // 检查是否支持kkFileView预览
            if (kkFileViewService.isSupportedFile(file.getFileName())) {
                // 生成文件访问URL
                String fileUrl = "/api/resource/preview-stream/" + fileId;
                // 使用kkFileView预览
                String kkPreviewUrl = kkFileViewService.generatePreviewUrl(fileUrl, file.getFileName());
                if (kkPreviewUrl != null) {
                    result.put("url", kkPreviewUrl);
                    result.put("previewType", "kkfileview");
                } else {
                    // kkFileView不可用时，使用本地预览
                    result.put("url", fileUrl);
                }
            } else {
                // 不支持的文件类型，使用本地预览
                result.put("url", "/api/resource/preview-stream/" + fileId);
            }
        } else {
            // 其他文件使用MinIO预签名URL
            String previewUrl = minioUtil.getPreviewUrl(file.getFilePath());
            result.put("url", previewUrl);
        }

        result.put("fileType", fileType);
        result.put("previewType", result.get("previewType") != null ? result.get("previewType") : previewType);
        result.put("fileName", file.getFileName());

        return result;
    }

    @Override
    public void approveFile(String fileId) {
        ResourceFile file = resourceFileRepository.findById(fileId)
                .orElseThrow(() -> new ServiceException(404, "文件不存在，无法审批"));
        file.setStatus(FileStatus.APPROVED); // 1=已审核
        resourceFileRepository.save(file);
    }

    @Override
    public void deleteFile(String fileId) {
        if (!resourceFileRepository.existsById(fileId)) {
            throw new ServiceException(404, "文件不存在，无法删除");
        }
        resourceFileRepository.deleteById(fileId);
    }

    @Override
    public long countAllFiles() {
        return resourceFileRepository.count();
    }

    @Override
    public List<ResourceFile> listAllFiles() {
        return resourceFileRepository.findAll();
    }
} 