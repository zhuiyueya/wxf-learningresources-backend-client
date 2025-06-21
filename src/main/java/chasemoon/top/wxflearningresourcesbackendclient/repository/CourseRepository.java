package chasemoon.top.wxflearningresourcesbackendclient.repository;

import chasemoon.top.wxflearningresourcesbackendclient.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);
    boolean existsByName(String name);
    // 可根据需要添加自定义查询方法
} 