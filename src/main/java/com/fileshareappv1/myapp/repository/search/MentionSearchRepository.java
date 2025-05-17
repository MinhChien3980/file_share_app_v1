package com.fileshareappv1.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.fileshareappv1.myapp.domain.Mention;
import com.fileshareappv1.myapp.repository.MentionRepository;
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
 * Spring Data Elasticsearch repository for the {@link Mention} entity.
 */
public interface MentionSearchRepository extends ElasticsearchRepository<Mention, Long>, MentionSearchRepositoryInternal {}

interface MentionSearchRepositoryInternal {
    Page<Mention> search(String query, Pageable pageable);

    Page<Mention> search(Query query);

    @Async
    void index(Mention entity);

    @Async
    void deleteFromIndexById(Long id);
}

class MentionSearchRepositoryInternalImpl implements MentionSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MentionRepository repository;

    MentionSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, MentionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Mention> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Mention> search(Query query) {
        SearchHits<Mention> searchHits = elasticsearchTemplate.search(query, Mention.class);
        List<Mention> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Mention entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Mention.class);
    }
}
