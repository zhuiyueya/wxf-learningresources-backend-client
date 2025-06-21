package chasemoon.top.wxflearningresourcesbackendclient.controller;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import chasemoon.top.wxflearningresourcesbackendclient.service.ResourceFileService;
import chasemoon.top.wxflearningresourcesbackendclient.common.Result;
import chasemoon.top.wxflearningresourcesbackendclient.util.MinioUtil;
import chasemoon.top.wxflearningresourcesbackendclient.service.KkFileViewService;
import chasemoon.top.wxflearningresourcesbackendclient.service.FileHelperService;
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
    private final ResourceFileService resourceFileService;
    private final MinioUtil minioUtil;
    private final KkFileViewService kkFileViewService;
    private final FileHelperService fileHelperService;

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
        return Result.success(resourceFileService.createPreview(fileId));
    }
    
    /**
     * 流式预览文件
     *
     * @param fileId   文件ID
     * @param fileName 文件名 (可选, 从URL路径中获取)
     * @return 文件流
     */
    @GetMapping(value = "/preview-stream/{fileId}/{fileName:.+}")
    public ResponseEntity<byte[]> previewStream(@PathVariable String fileId,
                                               @PathVariable(required = false) String fileName) throws Exception {
        log.info("开始流式预览文件，fileId: {}, fileName: {}", fileId, fileName);
        ResourceFile file = resourceFileService.getFileDetail(fileId);
        if (file == null) {
            log.warn("文件不存在，fileId: {}", fileId);
            return ResponseEntity.notFound().build();
        }
        byte[] data = resourceFileService.downloadFile(fileId);
        
        // 如果URL中没有文件名，则使用数据库中的文件名
        String actualFileName = fileName != null ? fileName : file.getFileName();
        
        // 根据文件类型设置正确的Content-Type
        String contentType = fileHelperService.getContentTypeForPreview(actualFileName);
        
        String encodedFileName = java.net.URLEncoder.encode(actualFileName, "UTF-8").replaceAll("\\+", "%20");
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
} 