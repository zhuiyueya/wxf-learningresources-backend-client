package chasemoon.top.wxflearningresourcesbackendclient.controller;

import chasemoon.top.wxflearningresourcesbackendclient.entity.Course;
import chasemoon.top.wxflearningresourcesbackendclient.service.CourseService;
import chasemoon.top.wxflearningresourcesbackendclient.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/list")
    public Result<List<Course>> list() {
        return Result.success(courseService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Course> get(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @PostMapping("/save")
    public Result<Course> save(@RequestBody Course course) {
        return Result.success(courseService.save(course));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success(null);
    }
} 