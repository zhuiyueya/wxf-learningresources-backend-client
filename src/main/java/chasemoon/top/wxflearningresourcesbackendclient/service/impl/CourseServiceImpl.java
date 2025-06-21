package chasemoon.top.wxflearningresourcesbackendclient.service.impl;

import chasemoon.top.wxflearningresourcesbackendclient.entity.Course;
import chasemoon.top.wxflearningresourcesbackendclient.exception.ServiceException;
import chasemoon.top.wxflearningresourcesbackendclient.repository.CourseRepository;
import chasemoon.top.wxflearningresourcesbackendclient.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
    @Transactional
    public Course save(Course course) {
        // 检查课程代码是否已存在
        if (course.getId() == null) {
            // 新增课程
            if (courseRepository.existsByCode(course.getCode())) {
                throw new ServiceException(409,"课程代码已存在：" + course.getCode());
            }
            if (courseRepository.existsByName(course.getName())) {
                throw new ServiceException(409,"课程名已存在：" + course.getName());
            }
            course.setCreateTime(new Date());
        } else {
            // 修改课程
            Course existingCourse = courseRepository.findById(course.getId())
                    .orElseThrow(() -> new ServiceException(404, "要修改的课程不存在"));

            // 检查课程代码是否被其他课程使用
            if (!existingCourse.getCode().equals(course.getCode()) &&
                courseRepository.existsByCode(course.getCode())) {
                throw new ServiceException(409, "课程代码已存在：" + course.getCode());
            }
            
            // 检查课程名是否被其他课程使用
            if (!existingCourse.getName().equals(course.getName()) &&
                courseRepository.existsByName(course.getName())) {
                throw new ServiceException(409, "课程名已存在：" + course.getName());
            }
        }
        
        course.setUpdateTime(new Date());
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ServiceException(404, "要删除的课程不存在");
        }
        courseRepository.deleteById(id);
    }
} 