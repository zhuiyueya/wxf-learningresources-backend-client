package chasemoon.top.wxflearningresourcesbackendclient.controller;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import chasemoon.top.wxflearningresourcesbackendclient.service.ResourceFileService;
import chasemoon.top.wxflearningresourcesbackendclient.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ResourceFileService resourceFileService;

    @PostMapping("/approve/{fileId}")
    public Result<Void> approve(@PathVariable String fileId) {
        resourceFileService.approveFile(fileId);
        return Result.success(null);
    }

    @DeleteMapping("/delete/{fileId}")
    public Result<Void> delete(@PathVariable String fileId) {
        resourceFileService.deleteFile(fileId);
        return Result.success(null);
    }

    @GetMapping("/stats")
    public Result<Long> count() {
        return Result.success(resourceFileService.countAllFiles());
    }

    @GetMapping("/listAll")
    public Result<List<ResourceFile>> listAll() {
        return Result.success(resourceFileService.listAllFiles());
    }
} 