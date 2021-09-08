package com.javatab.springelasticdemo.api;

import com.javatab.springelasticdemo.model.Movie;
import com.javatab.springelasticdemo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public Mono<IndexResponse> createDocument(@RequestBody Movie movie) {
        return movieService.createMovieDocument(movie);
    }

    @GetMapping("/{movie-name}")
    public Mono<Movie> findById(@PathVariable("movie-name") String movieName) {
        return movieService.findByMovieName(movieName);
    }

   /* @PutMapping
    public ResponseEntity<String> updateProfile(@RequestBody Movie movie) throws Exception {
        return new ResponseEntity<>(movieService.updateMovie(movie), HttpStatus.CREATED);
    }*/

    @GetMapping
    public Flux<Movie> findAll() {
        return movieService.findAll();
    }

    @GetMapping(value = "/search")
    public Flux<Movie> search(
            @RequestParam(value = "genre") String genre) {
        return movieService.searchByGenre(genre);
    }

}
