package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {
}
