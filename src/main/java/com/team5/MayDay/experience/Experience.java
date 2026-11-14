package com.team5.MayDay.experience;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experiences")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(length = 3000, nullable = false)
    private String content;

    @Column(nullable = false)
    private String category;

    private LocalDateTime createdAt;

    protected Experience() {
    }

    public Experience(String type, String content, String category) {
        this.type = type;
        this.content = content;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}