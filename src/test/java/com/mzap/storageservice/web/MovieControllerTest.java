package com.mzap.storageservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzap.storageservice.entity.Movie;
import com.mzap.storageservice.service.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieService service;

    private static Movie createMovie(String title) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre("Genre");
        movie.setReleaseYear(2025);
        return movie;
    }

    @Test
    @DisplayName("GET /movies returns paged content")
    void getMoviesPage() throws Exception {
        Pageable pageable = PageRequest.of(2, 5);
        List<Movie> list = List.of(createMovie("Movie 1"), createMovie("Movie 2"));
        Page<Movie> page = new PageImpl<>(list, pageable, 12);
        when(service.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/movies")
                        .param("page", "2")
                        .param("size", "5")
                        .header("X-Correlation-ID", "test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageNumber", is(2)))
                .andExpect(jsonPath("$.pageable.pageSize", is(5)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(12)));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAll(captor.capture());
        Pageable returnedPageable = captor.getValue();

        assertEquals(2, returnedPageable.getPageNumber());
        assertEquals(5, returnedPageable.getPageSize());
    }

    @Test
    @DisplayName("GET /movies/{id} returns 200 with body")
    void getByIdFound() throws Exception {
        Movie movie = createMovie("Movie 1");
        when(service.getById(10L)).thenReturn(Optional.of(movie));

        mockMvc.perform(get("/movies/10").header("X-Correlation-ID", "test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Movie 1")))
                .andExpect(jsonPath("$.genre", is("Genre")))
                .andExpect(jsonPath("$.releaseYear", is(2025)));
    }

    @Test
    @DisplayName("GET /movies/{id} returns 404 when not found")
    void getByIdNotFound() throws Exception {
        when(service.getById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/movies/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /movies returns 201 with created entity")
    void createMovie() throws Exception {
        Movie movie = createMovie("New Movie");
        when(service.create(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Movie")));

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(service).create(captor.capture());
        assertEquals("New Movie", captor.getValue().getTitle());
    }

    @Test
    @DisplayName("PUT /movies/{id} returns 200 when update succeeds")
    void updateMovieOk() throws Exception {
        Movie movie = createMovie("Updated Movie");
        when(service.update(eq(5L), any(Movie.class))).thenAnswer(inv -> Optional.of(inv.getArgument(1)));

        mockMvc.perform(put("/movies/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Movie")));
    }

    @Test
    @DisplayName("PUT /movies/{id} returns 404 when service returns Not Found")
    void updateMovieNotFound() throws Exception {
        when(service.update(eq(9L), any(Movie.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/movies/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMovie("X"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /movies/{id} returns 204 and calls service.delete")
    void deleteMovie() throws Exception {
        mockMvc.perform(delete("/movies/7"))
                .andExpect(status().isNoContent());
        verify(service).delete(7L);
    }

    @Test
    @DisplayName("GET /movies/dev/chaos returns 200 when errorRate=0")
    void chaosEndpointOK() throws Exception {
        mockMvc.perform(get("/movies/dev/chaos").param("errorRate", "0").param("delay", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ok")));
    }

    @Test
    @DisplayName("GET /movies/dev/chaos returns 500 when errorRate=1")
    void chaosEndpointError() throws Exception {
        mockMvc.perform(get("/movies/dev/chaos").param("errorRate", "1").param("delay", "0"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Simulated error")));
    }
}
