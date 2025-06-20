package chasemoon.top.wxflearningresourcesbackendclient.controller;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import chasemoon.top.wxflearningresourcesbackendclient.repository.ResourceFileRepository;
import chasemoon.top.wxflearningresourcesbackendclient.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ResourceFileRepository resourceFileRepository;

    @PostMapping("/approve/{fileId}")
    public Result<Void> approve(@PathVariable String fileId) {
        resourceFileRepository.findById(fileId).ifPresent(file -> {
            file.setStatus((byte)1); // 1=已审核
            resourceFileRepository.save(file);
        });
        return Result.success(null);
    }

    @DeleteMapping("/delete/{fileId}")
    public Result<Void> delete(@PathVariable String fileId) {
        resourceFileRepository.deleteById(fileId);
        return Result.success(null);
    }

    @GetMapping("/stats")
    public Result<Long> count() {
        return Result.success(resourceFileRepository.count());
    }

    @GetMapping("/listAll")
    public Result<List<ResourceFile>> listAll() {
        return Result.success(resourceFileRepository.findAll());
    }
} 