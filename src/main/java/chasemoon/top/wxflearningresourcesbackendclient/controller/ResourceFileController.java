package chasemoon.top.wxflearningresourcesbackendclient.controller;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import chasemoon.top.wxflearningresourcesbackendclient.service.ResourceFileService;
import chasemoon.top.wxflearningresourcesbackendclient.common.Result;
import chasemoon.top.wxflearningresourcesbackendclient.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceFileController {
    @Autowired
    @Qualifier("ResourceFileServiceImpl")
    private ResourceFileService resourceFileService;
    private final MinioUtil minioUtil;

    @PostMapping("/upload")
    public Result<String> upload(@RequestPart("file") MultipartFile file, @ModelAttribute ResourceFile meta) throws Exception {
        log.info("开始上传文件 - 原始文件名: {}, 大小: {}, Content-Type: {}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        log.info("元数据: {}", meta);
        
        String result = resourceFileService.uploadFile(file, meta);
        log.info("上传完成，返回结果: {}", result);
        return Result.success(result);
    }

    @GetMapping("/detail/{fileId}")
    public Result<ResourceFile> detail(@PathVariable String fileId) {
        return Result.success(resourceFileService.getFileDetail(fileId));
    }

    @GetMapping("/list")
    public Result<Page<ResourceFile>> list(@RequestParam(required = false) Long courseId,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(resourceFileService.listFiles(courseId, keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable String fileId) throws Exception {
        ResourceFile file = resourceFileService.getFileDetail(fileId);
        String fileName = file.getFileName();
        String minioObjectName = file.getFilePath();
        byte[] data = resourceFileService.downloadFile(fileId);
        // 获取MinIO对象的Content-Type
        String contentType = "application/octet-stream";
        try {
            contentType = minioUtil.getStat(minioObjectName).contentType();
        } catch (Exception e) {
            // ignore, fallback to octet-stream
        }
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream");
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/preview/{fileId}")
    public Result<Map<String, Object>> preview(@PathVariable String fileId) throws Exception {
        log.info("开始预览文件，fileId: {}", fileId);
        
        ResourceFile file = resourceFileService.getFileDetail(fileId);
        if (file == null) {
            log.warn("文件不存在，fileId: {}", fileId);
            return Result.error(404,"文件不存在");
        }
        
        log.info("找到文件: {}", file);
        
        String fileName = file.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("文件名为空，使用默认处理");
            fileName = "unknown";
        }
        
        String fileType = getFileType(fileName);
        String previewType = getPreviewType(fileName);
        
        log.info("初步识别 - 文件名: {}, 文件类型: {}, 预览类型: {}", fileName, fileType, previewType);
        
        // 如果通过文件名无法识别，尝试通过Content-Type识别
        if ("unknown".equals(fileType)) {
            try {
                String contentType = minioUtil.getStat(file.getFilePath()).contentType();
                fileType = getFileTypeFromContentType(contentType);
                previewType = getPreviewTypeFromFileType(fileType);
                log.info("通过Content-Type识别文件类型: {} -> {}", contentType, fileType);
            } catch (Exception e) {
                log.warn("无法获取Content-Type: {}", e.getMessage());
            }
        }
        
        log.info("最终结果 - 文件名: {}, 文件类型: {}, 预览类型: {}", fileName, fileType, previewType);
        
        Map<String, Object> result = new HashMap<>();
        
        // 根据预览类型设置不同的URL
        if ("office".equals(previewType)) {
            // Office文档使用本地预览URL
            result.put("url", "/api/resource/preview-stream/" + fileId);
        } else {
            // 其他文件使用MinIO预签名URL
            String previewUrl = minioUtil.getPreviewUrl(file.getFilePath());
            result.put("url", previewUrl);
        }
        
        result.put("fileType", fileType);
        result.put("previewType", previewType);
        result.put("fileName", file.getFileName());
        
        return Result.success(result);
    }
    
    @GetMapping("/preview-stream/{fileId}")
    public ResponseEntity<byte[]> previewStream(@PathVariable String fileId) throws Exception {
        log.info("开始流式预览文件，fileId: {}", fileId);
        ResourceFile file = resourceFileService.getFileDetail(fileId);
        if (file == null) {
            log.warn("文件不存在，fileId: {}", fileId);
            return ResponseEntity.notFound().build();
        }
        byte[] data = resourceFileService.downloadFile(fileId);
        
        // 根据文件类型设置正确的Content-Type
        String contentType = getContentTypeForPreview(file.getFileName());
        
        String encodedFileName = java.net.URLEncoder.encode(file.getFileName(), "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
    
    /**
     * 根据文件名获取预览用的Content-Type
     */
    private String getContentTypeForPreview(String fileName) {
        if (fileName == null) return "application/octet-stream";
        
        String lowerFileName = fileName.toLowerCase();
        
        // Office文档类型
        if (lowerFileName.endsWith(".doc")) return "application/msword";
        if (lowerFileName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowerFileName.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lowerFileName.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lowerFileName.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lowerFileName.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        
        // 其他类型
        if (lowerFileName.endsWith(".pdf")) return "application/pdf";
        if (lowerFileName.endsWith(".txt")) return "text/plain";
        if (lowerFileName.matches(".*\\.(jpg|jpeg)$")) return "image/jpeg";
        if (lowerFileName.endsWith(".png")) return "image/png";
        if (lowerFileName.endsWith(".gif")) return "image/gif";
        if (lowerFileName.endsWith(".bmp")) return "image/bmp";
        if (lowerFileName.endsWith(".webp")) return "image/webp";
        
        return "application/octet-stream";
    }
    
    private String getFileType(String fileName) {
        log.debug("识别文件类型，文件名: {}", fileName);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("文件名为空");
            return "unknown";
        }
        
        String lowerFileName = fileName.toLowerCase().trim();
        
        if (lowerFileName.endsWith(".pdf")) {
            log.debug("识别为PDF文件");
            return "pdf";
        }
        if (lowerFileName.endsWith(".doc") || lowerFileName.endsWith(".docx")) {
            log.debug("识别为Word文件");
            return "word";
        }
        if (lowerFileName.endsWith(".ppt") || lowerFileName.endsWith(".pptx")) {
            log.debug("识别为PowerPoint文件");
            return "powerpoint";
        }
        if (lowerFileName.endsWith(".xls") || lowerFileName.endsWith(".xlsx")) {
            log.debug("识别为Excel文件");
            return "excel";
        }
        if (lowerFileName.endsWith(".txt")) {
            log.debug("识别为文本文件");
            return "text";
        }
        if (lowerFileName.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            log.debug("识别为图片文件");
            return "image";
        }
        if (lowerFileName.matches(".*\\.(mp4|avi|mov|wmv|flv|webm)$")) {
            log.debug("识别为视频文件");
            return "video";
        }
        if (lowerFileName.matches(".*\\.(mp3|wav|flac|aac)$")) {
            log.debug("识别为音频文件");
            return "audio";
        }
        
        log.warn("无法识别的文件类型: {}", fileName);
        return "unknown";
    }
    
    private String getPreviewType(String fileName) {
        String fileType = getFileType(fileName);
        return getPreviewTypeFromFileType(fileType);
    }
    
    private String getPreviewTypeFromFileType(String fileType) {
        log.debug("根据文件类型 {} 确定预览类型", fileType);
        
        switch (fileType) {
            case "pdf":
                log.debug("预览类型: pdf");
                return "pdf";
            case "image":
                log.debug("预览类型: image");
                return "image";
            case "video":
                log.debug("预览类型: video");
                return "video";
            case "audio":
                log.debug("预览类型: audio");
                return "audio";
            case "text":
                log.debug("预览类型: text");
                return "text";
            case "word":
            case "powerpoint":
            case "excel":
                log.debug("预览类型: office");
                return "office";
            default:
                log.debug("预览类型: download (未知类型)");
                return "download";
        }
    }
    
    private String getFileTypeFromContentType(String contentType) {
        if (contentType == null) return "unknown";
        
        contentType = contentType.toLowerCase();
        if (contentType.contains("pdf")) return "pdf";
        if (contentType.contains("msword") || contentType.contains("wordprocessingml")) return "word";
        if (contentType.contains("presentation") || contentType.contains("powerpoint")) return "powerpoint";
        if (contentType.contains("spreadsheet") || contentType.contains("excel")) return "excel";
        if (contentType.contains("text/plain")) return "text";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        if (contentType.startsWith("audio/")) return "audio";
        
        return "unknown";
    }
} 