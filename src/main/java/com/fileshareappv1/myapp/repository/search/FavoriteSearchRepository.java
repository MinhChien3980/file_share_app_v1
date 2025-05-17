package com.fileshareappv1.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.fileshareappv1.myapp.domain.Favorite;
import com.fileshareappv1.myapp.repository.FavoriteRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Favorite} entity.
 */
public interface FavoriteSearchRepository extends ElasticsearchRepository<Favorite, Long>, FavoriteSearchRepositoryInternal {}

interface FavoriteSearchRepositoryInternal {
    Stream<Favorite> search(String query);

    Stream<Favorite> search(Query query);

    @Async
    void index(Favorite entity);

    @Async
    void deleteFromIndexById(Long id);
}

class FavoriteSearchRepositoryInternalImpl implements FavoriteSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final FavoriteRepository repository;

    FavoriteSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, FavoriteRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Favorite> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Favorite> search(Query query) {
        return elasticsearchTemplate.search(query, Favorite.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Favorite entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Favorite.class);
    }
}
