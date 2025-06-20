package chasemoon.top.wxflearningresourcesbackendclient.repository;

import chasemoon.top.wxflearningresourcesbackendclient.entity.ResourceFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceFileRepository extends JpaRepository<ResourceFile, String> {
    // 可根据需要添加自定义查询方法

    // 按文件名、描述、课程名模糊搜索
    @Query("SELECT r FROM ResourceFile r LEFT JOIN Course c ON r.courseId = c.id " +
           "WHERE (:courseId IS NULL OR r.courseId = :courseId) " +
           "AND (COALESCE(:keyword, '') = '' " +
           "OR r.fileName LIKE %:keyword% " +
           "OR r.fileCover LIKE %:keyword% " +
           "OR c.name LIKE %:keyword%)")
    Page<ResourceFile> search(@Param("courseId") Long courseId, @Param("keyword") String keyword, Pageable pageable);
} 