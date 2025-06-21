package chasemoon.top.wxflearningresourcesbackendclient.entity;

import chasemoon.top.wxflearningresourcesbackendclient.entity.enums.DeleteFlag;
import chasemoon.top.wxflearningresourcesbackendclient.entity.enums.FileStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.Date;

@Data
@Entity
@Table(name = "resource")
public class ResourceFile {
    @Id
    @Column(length = 64)
    private String fileId;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(length = 64)
    private String fileMd5;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 255)
    private String fileCover;

    @Column(nullable = false, length = 255)
    private String filePath;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTime;

    @Column(nullable = false)
    private Byte folderType;

    @Column(nullable = false)
    private Byte fileCategory;

    @Column(nullable = false)
    private Byte fileType;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private FileStatus status = FileStatus.UNREVIEWED;

    @Temporal(TemporalType.TIMESTAMP)
    private Date recoveryTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private DeleteFlag delFlag = DeleteFlag.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Course course;

    public void setCourseId(Long courseId) {
        if (this.course == null) {
            this.course = new Course();
        }
        this.course.setId(courseId);
    }
} 