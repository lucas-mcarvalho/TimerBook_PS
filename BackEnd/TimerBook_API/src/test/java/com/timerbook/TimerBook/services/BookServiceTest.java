package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.BookDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.exception.BookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    FileStorageService fileStorageService;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, userRepository, fileStorageService);
    }

    @Test
    void findAll() {
        Book book = new Book();
        book.setId(1L);
        book.setName("Livro Teste");

        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Livro Teste", result.get(0).getName());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void findById() {
        Book book = new Book();
        book.setId(1L);
        book.setName("Livro Teste");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Livro Teste", result.getName());
    }

    @Test
    void findByIdShouldThrowWhenBookDoesNotExist() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.findById(1L));
        assertEquals("Livro não encontrado", exception.getMessage());
    }

    @Test
    void create() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        MockMultipartFile coverFile = new MockMultipartFile("cover", "capa.jpg", "image/jpeg", "imagem".getBytes());
        MockMultipartFile dataFile = new MockMultipartFile("data", "livro.pdf", "application/pdf", "pdf".getBytes());

        BookDTO dto = new BookDTO();
        dto.setName("Novo Livro");
        dto.setDescription("Descrição do Livro");
        dto.setCover(coverFile);
        dto.setData(dataFile);

        Book savedBook = new Book();
        savedBook.setId(10L);
        savedBook.setName("Novo Livro");
        savedBook.setCoverUrl("covers/capa.jpg");
        savedBook.setDataPath("pdfs/livro.pdf");
        savedBook.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(coverFile, "covers")).thenReturn("covers/capa.jpg");
        when(fileStorageService.storeFile(dataFile, "pdfs")).thenReturn("pdfs/livro.pdf");
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.create(userId, dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Novo Livro", result.getName());
        assertEquals("covers/capa.jpg", result.getCoverUrl());
        assertEquals("pdfs/livro.pdf", result.getDataPath());

        verify(userRepository, times(1)).findById(userId);
        verify(fileStorageService, times(1)).storeFile(coverFile, "covers");
        verify(fileStorageService, times(1)).storeFile(dataFile, "pdfs");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createShouldSaveBookWithoutOptionalFiles() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        BookDTO dto = new BookDTO();
        dto.setName("Livro sem arquivos");
        dto.setDescription("Descrição");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.create(userId, dto);

        assertEquals("Livro sem arquivos", result.getName());
        assertEquals("Descrição", result.getDescription());
        assertNull(result.getCoverUrl());
        assertNull(result.getDataPath());
        assertEquals(user, result.getUser());
        verify(fileStorageService, never()).storeFile(any(), anyString());
    }

    @Test
    void createShouldThrowWhenUserDoesNotExist() {
        BookDTO dto = new BookDTO();
        dto.setName("Novo Livro");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookService.create(99L, dto));
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update() {
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setName("Livro Antigo");
        existingBook.setCoverUrl("old_cover.jpg");
        existingBook.setDataPath("old_book.pdf");

        MockMultipartFile newCover = new MockMultipartFile("cover", "nova_capa.jpg", "image/jpeg", "img".getBytes());
        MockMultipartFile newData = new MockMultipartFile("data", "novo_livro.pdf", "application/pdf", "pdf".getBytes());

        BookDTO dto = new BookDTO();
        dto.setName("Livro Atualizado");
        dto.setDescription("Nova Descrição");
        dto.setCover(newCover);
        dto.setData(newData);

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setName("Livro Atualizado");
        updatedBook.setCoverUrl("covers/nova_capa.jpg");
        updatedBook.setDataPath("pdfs/novo_livro.pdf");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(fileStorageService.storeFile(newCover, "covers")).thenReturn("covers/nova_capa.jpg");
        when(fileStorageService.storeFile(newData, "pdfs")).thenReturn("pdfs/novo_livro.pdf");
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        Book result = bookService.update(bookId, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Livro Atualizado", result.getName());

        verify(bookRepository, times(1)).findById(bookId);
        verify(fileStorageService, times(1)).deleteFile("old_cover.jpg");
        verify(fileStorageService, times(1)).deleteFile("old_book.pdf");
        verify(fileStorageService, times(1)).storeFile(newCover, "covers");
        verify(fileStorageService, times(1)).storeFile(newData, "pdfs");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateShouldKeepExistingFilesWhenNoNewFilesAreSent() {
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setName("Livro Antigo");
        existingBook.setDescription("Antiga");
        existingBook.setCoverUrl("old_cover.jpg");
        existingBook.setDataPath("old_book.pdf");

        BookDTO dto = new BookDTO();
        dto.setName("Livro Atualizado");
        dto.setDescription("Nova");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.update(bookId, dto);

        assertEquals("Livro Atualizado", result.getName());
        assertEquals("Nova", result.getDescription());
        assertEquals("old_cover.jpg", result.getCoverUrl());
        assertEquals("old_book.pdf", result.getDataPath());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(fileStorageService, never()).storeFile(any(), anyString());
    }

    @Test
    void delete() {
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setCoverUrl("cover.jpg");
        book.setDataPath("livro.pdf");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.delete(bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(fileStorageService, times(1)).deleteFile("cover.jpg");
        verify(fileStorageService, times(1)).deleteFile("livro.pdf");
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void findByUserIdShouldDelegateToRepository() {
        Book book = new Book();
        book.setId(1L);
        book.setName("Livro do usuário");
        when(bookRepository.findByUserId(10L)).thenReturn(List.of(book));

        List<Book> result = bookService.findByUserId(10L);

        assertEquals(1, result.size());
        assertEquals("Livro do usuário", result.get(0).getName());
        verify(bookRepository).findByUserId(10L);
    }
}
