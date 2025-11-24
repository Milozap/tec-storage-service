package com.mzap.storageservice.service;

import com.mzap.storageservice.entity.Movie;
import com.mzap.storageservice.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    @Mock
    private MovieRepository repository;

    @InjectMocks
    private MovieService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Movie createMovie(String title) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre("Genre");
        movie.setReleaseYear(2025);
        return movie;
    }

    @Test
    @DisplayName("getAll returns page with movies")
    void getAllReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Movie> items = List.of(createMovie("Movie 1"), createMovie("Movie 2"));
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(items, pageable, items.size()));

        Page<Movie> result = service.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Movie 1", result.getContent().getFirst().getTitle());
        assertEquals("Movie 2", result.getContent().getLast().getTitle());
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("getById returns the movie when present")
    void getByIdPresent() {
        Movie movie = createMovie("New Movie");
        when(repository.findById(1L)).thenReturn(Optional.of(movie));

        Optional<Movie> result = service.getById(1L);
        assertTrue(result.isPresent());
        assertEquals("New Movie", result.get().getTitle());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("getById returns empty when the movie is not present")
    void getByIdEmpty() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        Optional<Movie> result = service.getById(42L);
        assertTrue(result.isEmpty());
        verify(repository).findById(42L);
    }

    @Test
    @DisplayName("create saves entity via repository")
    void createSaves() {
        Movie input = createMovie("New Movie");
        when(repository.save(input)).thenReturn(input);

        Movie result = service.create(input);
        assertSame(input, result);
        verify(repository).save(input);
    }

    @Test
    @DisplayName("update modifies fields and saves when the movie exists")
    void updateSuccess() {
        Movie existing = createMovie("Old Movie");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));

        Movie patch = createMovie("Updated Movie");
        patch.setGenre("Drama");
        patch.setReleaseYear(1999);

        when(repository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Movie> result = service.update(5L, patch);

        assertTrue(result.isPresent());
        Movie saved = result.get();
        assertEquals("Updated Movie", saved.getTitle());
        assertEquals("Drama", saved.getGenre());
        assertEquals(1999, saved.getReleaseYear());

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(repository).save(captor.capture());
        Movie toSave = captor.getValue();
        assertEquals("Updated Movie", toSave.getTitle());
    }

    @Test
    @DisplayName("update throws when the movie is not found")
    void updateNotFound() {
        Movie movie = createMovie("X");

        when(repository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.update(999L, movie)
        );
        assertTrue(exception.getMessage().contains("999"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete delegates to repository.deleteById")
    void deleteById() {
        service.delete(7L);
        verify(repository).deleteById(7L);
    }
}
