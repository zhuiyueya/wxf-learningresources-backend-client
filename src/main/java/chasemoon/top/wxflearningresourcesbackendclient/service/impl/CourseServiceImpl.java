package chasemoon.top.wxflearningresourcesbackendclient.service.impl;

import chasemoon.top.wxflearningresourcesbackendclient.entity.Course;
import chasemoon.top.wxflearningresourcesbackendclient.repository.CourseRepository;
import chasemoon.top.wxflearningresourcesbackendclient.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public List<Course> listAll() {
        return courseRepository.findAll();
    }

    @Override
    public Course getById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }
} 