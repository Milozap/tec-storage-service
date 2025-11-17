package com.mzap.storageservice.web;

import com.mzap.storageservice.entity.Movie;
import com.mzap.storageservice.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Movie> getAll(
            Pageable pageable,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        logger.info("GET /movies correlationId={}", correlationId);
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> get(
            @PathVariable Long id,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        logger.info("GET /movies/{} correlationId={}", id, correlationId);
        return service
                .getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Movie> create(
            @RequestBody Movie movie,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        logger.info("POST /movies correlationId={}", correlationId);
        Movie created = service.create(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movie> create(
            @PathVariable Long id,
            @RequestBody Movie movie,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        logger.info("PUT /movies/{} correlationId={}", id, correlationId);
        return service
                .update(id, movie)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        logger.info("DELETE /movies/{} correlationId={}", id, correlationId);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
