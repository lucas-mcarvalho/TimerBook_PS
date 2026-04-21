package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book,Long> {
    List<Book> findByUserId(Long userId);
}
