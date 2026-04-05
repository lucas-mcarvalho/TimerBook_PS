package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.services.exception.BookException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> findALl(){
        return bookRepository.findAll();
    }

    public  void create(Book book){
        bookRepository.save(book);
    }

    public void update(Long id,Book book){

            Book book1 = findById(id);
            book1.setName(book.getName());
            book1.setCoverUrl(book.getCoverUrl());
            book1.setDataPath(book.getDataPath());
            book1.setDescription(book.getDescription());
            bookRepository.save(book1);
    }

    public void delete(Long id){
       findById(id);
       bookRepository.deleteById(id);
    }

    public Book findById(Long id){
        Optional<Book> obj = bookRepository.findById(id);
        return obj.orElseThrow(() -> new BookException("Id nao encontrado"));
    }
}
