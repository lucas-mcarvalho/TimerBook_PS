package com.timerbook.TimerBook.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "reading")
public class Reading implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer currentPage;

    private LocalDateTime startedAt;
    
    private LocalDateTime finishedAt;


    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ReadingSession> sessions = new ArrayList<>();
    public Reading() {}

    public Reading(Long id, Book book, Integer currentPage, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.id = id;
        this.book = book;
        this.currentPage = currentPage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public List<ReadingSession> getSessions() {
        return sessions;
    }
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reading reading = (Reading) o;
        return Objects.equals(id, reading.id) && Objects.equals(book, reading.book) && Objects.equals(user, reading.user) && Objects.equals(currentPage, reading.currentPage) && Objects.equals(startedAt, reading.startedAt) && Objects.equals(finishedAt, reading.finishedAt) && Objects.equals(sessions, reading.sessions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, book, user, currentPage, startedAt, finishedAt, sessions);
    }
}
