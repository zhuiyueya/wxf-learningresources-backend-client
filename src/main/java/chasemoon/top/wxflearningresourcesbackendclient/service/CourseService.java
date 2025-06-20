package chasemoon.top.wxflearningresourcesbackendclient.service;

import chasemoon.top.wxflearningresourcesbackendclient.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> listAll();
    Course getById(Long id);
    Course save(Course course);
    void delete(Long id);
} 