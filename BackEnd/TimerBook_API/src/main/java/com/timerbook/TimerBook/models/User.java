package com.timerbook.TimerBook.models;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    public static final int DEFAULT_DAILY_READING_GOAL_MINUTES = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String photopath;
    private String refreshToken;
    private Boolean enabled = false;
    @Column(name = "daily_reading_goal_minutes", nullable = false)
    private Integer dailyReadingGoalMinutes = DEFAULT_DAILY_READING_GOAL_MINUTES;
    @Column(name = "last_reading_reminder_sent_at")
    private LocalDateTime lastReadingReminderSentAt;
    @Column(name = "subscription_plan", nullable = false)
    private String subscriptionPlan = "FREE";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Book> books = new ArrayList<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tb_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User(){

    }

    public User(Long id, String username, String email, String password, String photopath) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.photopath = photopath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotopath() {
        return photopath;
    }

    public void setPhotopath(String photopath) {
        this.photopath = photopath;
    }

    public List<Book> getBooks() {
        return books;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getDailyReadingGoalMinutes() {
        return dailyReadingGoalMinutes;
    }

    public void setDailyReadingGoalMinutes(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }

    public LocalDateTime getLastReadingReminderSentAt() {
        return lastReadingReminderSentAt;
    }

    public void setLastReadingReminderSentAt(LocalDateTime lastReadingReminderSentAt) {
        this.lastReadingReminderSentAt = lastReadingReminderSentAt;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public boolean isPaidUser() {
        return "PAID".equals(subscriptionPlan);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(photopath, user.photopath) && Objects.equals(refreshToken, user.refreshToken) && Objects.equals(enabled, user.enabled) && Objects.equals(dailyReadingGoalMinutes, user.dailyReadingGoalMinutes) && Objects.equals(lastReadingReminderSentAt, user.lastReadingReminderSentAt) && Objects.equals(subscriptionPlan, user.subscriptionPlan) && Objects.equals(books, user.books) && Objects.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, password, photopath, refreshToken, enabled, dailyReadingGoalMinutes, lastReadingReminderSentAt, subscriptionPlan, books, roles);
    }
}
