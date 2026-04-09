package com.timerbook.TimerBook.models;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private String dataPath;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Book(){

    }

    public Book(Long id, String name, String description, String coverUrl, String dataPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.coverUrl = coverUrl;
        this.dataPath = dataPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) return false;
        return Objects.equals(id, book.id) && Objects.equals(name, book.name) && Objects.equals(description, book.description) && Objects.equals(coverUrl, book.coverUrl) && Objects.equals(dataPath, book.dataPath) && Objects.equals(user, book.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, coverUrl, dataPath, user);
    }
}
