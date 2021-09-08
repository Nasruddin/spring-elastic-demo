package com.javatab.springelasticdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatab.springelasticdemo.model.Movie;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieService {

    public static final String MOVIE_STORE_INDEX = "movie-store";
    private final ReactiveElasticsearchClient client;
    private final ObjectMapper objectMapper;

    public Mono<IndexResponse> createMovieDocument(Movie movie) {
        String id = UUID.randomUUID().toString();
        Map documentMapper = objectMapper.convertValue(movie,
                Map.class);
        return client.index(indexRequest -> indexRequest.index("movie-store").id(id).source(documentMapper));
    }

    public Mono<Movie> findById(String id) {
        GetRequest getRequest = new GetRequest("movie-store", id);
        Mono<Map<String, Object>> resultMap = client.get(getRequest).map(GetResult::getSource);
        return Mono.just(objectMapper
                .convertValue(resultMap, Movie.class));
    }

    public Mono<Movie> findByMovieName(String movieName) {
        GetRequest getRequest = new GetRequest("movie-store", movieName);
        Mono<Map<String, Object>> resultMap = client.get(getRequest).map(GetResult::getSource);
        return Mono.just(objectMapper
                .convertValue(resultMap, Movie.class));
    }

    public Mono<UpdateResponse> updateMovie(Movie movie) {
        Map documentMapper =
                objectMapper.convertValue(movie, Map.class);
        UpdateRequest updateRequest = new UpdateRequest(
                MOVIE_STORE_INDEX,
                movie.getId())
                .doc(documentMapper);

        return client.update(updateRequest);
    }

    public Flux<Movie> findAll() {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        Flux<SearchHit> searchResponse =
                client.search(searchRequest);

        return getSearchResult(searchResponse);
    }

    public Flux<Movie> searchByGenre(String genre) {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("genre.name", genre);
        searchSourceBuilder.query(matchQueryBuilder);

        searchRequest.source(searchSourceBuilder);

        Flux<SearchHit> response =
                client.search(searchRequest);

        return getSearchResult(response);
    }

    private Flux<Movie> getSearchResult(Flux<SearchHit> response) {
        return response.map(documentField -> objectMapper.convertValue(documentField.getSourceAsMap(), Movie.class));

    }
}
