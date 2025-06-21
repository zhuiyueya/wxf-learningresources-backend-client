package chasemoon.top.wxflearningresourcesbackendclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileHelperService {

    public String getFileType(String fileName) {
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

    public String getPreviewTypeFromFileType(String fileType) {
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

    public String getFileTypeFromContentType(String contentType) {
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

    public String getContentTypeForPreview(String fileName) {
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
} 