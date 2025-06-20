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
        return Result.success(resourceFileService.uploadFile(file, meta));
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
    public Result<String> preview(@PathVariable String fileId) throws Exception {
        return Result.success(resourceFileService.getPreviewUrl(fileId));
    }
} 