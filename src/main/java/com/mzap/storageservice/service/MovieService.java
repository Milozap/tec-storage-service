package com.mzap.storageservice.service;

import com.mzap.storageservice.entity.Movie;
import com.mzap.storageservice.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    public Page<Movie> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Movie> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Movie create(Movie movie) {
        return repository.save(movie);
    }

    @Transactional
    public Optional<Movie> update(Long id, Movie movie) {
        Movie original = repository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Movie with id not found: " + id));

        original.setTitle(movie.getTitle());
        original.setGenre(movie.getGenre());
        original.setReleaseYear(movie.getReleaseYear());

        return Optional.of(repository.save(original));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
