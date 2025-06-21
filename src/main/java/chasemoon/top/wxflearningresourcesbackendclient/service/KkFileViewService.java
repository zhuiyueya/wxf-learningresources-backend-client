package chasemoon.top.wxflearningresourcesbackendclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class KkFileViewService {
    
    @Value("${kkfileview.url:http://localhost:8012}")
    private String kkFileViewUrl;

    @Value("${backend.url:http://localhost:8080}")
    private String backendUrl;
    
    @Value("${kkfileview.enabled:true}")
    private boolean kkFileViewEnabled;
    
    /**
     * 生成kkFileView预览URL
     * @param fileUrl 文件URL
     * @param fileName 文件名
     * @return 预览URL
     */
    public String generatePreviewUrl(String fileUrl, String fileName) {
        if (!kkFileViewEnabled) {
            log.warn("kkFileView未启用");
            return null;
        }
        
        try {
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            String fullFileUrl = backendUrl + fileUrl + "/" + encodedFileName;

            // 使用Base64编码URL
            String encodedUrl = Base64.getEncoder().encodeToString(fullFileUrl.getBytes(StandardCharsets.UTF_8));
            
            // 构建预览URL
            String previewUrl = String.format("%s/onlinePreview?url=%s", kkFileViewUrl, encodedUrl);
            
            log.info("生成kkFileView预览URL: {}", previewUrl);
            return previewUrl;
        } catch (Exception e) {
            log.error("生成kkFileView预览URL失败", e);
            return null;
        }
    }
    
    /**
     * 检查文件是否支持预览
     * @param fileName 文件名
     * @return 是否支持
     */
    public boolean isSupportedFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        
        // Office文档
        if (lowerFileName.endsWith(".doc") || lowerFileName.endsWith(".docx")) return true;
        if (lowerFileName.endsWith(".xls") || lowerFileName.endsWith(".xlsx")) return true;
        if (lowerFileName.endsWith(".ppt") || lowerFileName.endsWith(".pptx")) return true;
        
        // PDF
        if (lowerFileName.endsWith(".pdf")) return true;
        
        // 图片
        if (lowerFileName.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|tiff|tif)$")) return true;
        
        // 文本文件
        if (lowerFileName.endsWith(".txt") || lowerFileName.endsWith(".md")) return true;
        
        // 代码文件
        if (lowerFileName.matches(".*\\.(java|py|js|html|css|xml|json|c|cpp|h|sql|sh|bat)$")) return true;
        
        return false;
    }
} 