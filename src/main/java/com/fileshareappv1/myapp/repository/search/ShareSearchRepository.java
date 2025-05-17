package com.fileshareappv1.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.fileshareappv1.myapp.domain.Share;
import com.fileshareappv1.myapp.repository.ShareRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Share} entity.
 */
public interface ShareSearchRepository extends ElasticsearchRepository<Share, Long>, ShareSearchRepositoryInternal {}

interface ShareSearchRepositoryInternal {
    Page<Share> search(String query, Pageable pageable);

    Page<Share> search(Query query);

    @Async
    void index(Share entity);

    @Async
    void deleteFromIndexById(Long id);
}

class ShareSearchRepositoryInternalImpl implements ShareSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ShareRepository repository;

    ShareSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, ShareRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Share> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Share> search(Query query) {
        SearchHits<Share> searchHits = elasticsearchTemplate.search(query, Share.class);
        List<Share> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Share entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Share.class);
    }
}
