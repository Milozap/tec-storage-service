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

import java.util.Random;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    private final MovieService service;
    private final Random random = new Random();

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Movie> getPage(
            Pageable pageable,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("GET /movies correlationId={}, page={}, size={}", correlationId, page, size);
        Pageable.ofSize(size);
        pageable.withPage(page);

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

    @GetMapping("/dev/chaos")
    public ResponseEntity<String> chaos(
            @RequestParam(name = "delay", defaultValue = "0") long delay,
            @RequestParam(name = "errorRate", defaultValue = "0.0") double errorRate,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId
    ) {
        logger.info("GET /movies/dev/chaos delay={} errorRate={} correlationId={}", delay, errorRate, correlationId);

        if(delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                logger.warn("Chaos sleep interrupted, correlationId={}", correlationId);
            }
        }

        if(errorRate > 0 && random.nextDouble() < errorRate) {
            logger.warn("Chaos endpoint: throwing simulated error, correlationId={}", correlationId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Simulated error from chaos endpoint");
        }

        return ResponseEntity.ok("Ok from chaos endpoint");
    }
}
