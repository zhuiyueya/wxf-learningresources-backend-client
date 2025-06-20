package chasemoon.top.wxflearningresourcesbackendclient.entity;

import jakarta.persistence.*;
import lombok.Data;
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

    @Column(nullable = false)
    private Byte status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date recoveryTime;

    @Column(nullable = false)
    private Byte delFlag = 0;

    @Column(nullable = false)
    private Long courseId;
} 