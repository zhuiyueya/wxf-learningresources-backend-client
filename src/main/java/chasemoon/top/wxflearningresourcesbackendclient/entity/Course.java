package chasemoon.top.wxflearningresourcesbackendclient.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "课程名不能为空")
    @Size(min = 2, max = 64, message = "课程名长度必须在2-64个字符之间")
    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @NotBlank(message = "课程代码不能为空")
    @Size(min = 2, max = 32, message = "课程代码长度必须在2-32个字符之间")
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Size(min = 2, max = 64, message = "教师姓名长度必须在2-64个字符之间")
    @Column(length = 64)
    private String teacher;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<ResourceFile> resourceFiles;
} 