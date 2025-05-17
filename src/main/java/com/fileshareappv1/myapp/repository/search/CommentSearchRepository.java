package com.fileshareappv1.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.fileshareappv1.myapp.domain.Comment;
import com.fileshareappv1.myapp.repository.CommentRepository;
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
 * Spring Data Elasticsearch repository for the {@link Comment} entity.
 */
public interface CommentSearchRepository extends ElasticsearchRepository<Comment, Long>, CommentSearchRepositoryInternal {}

interface CommentSearchRepositoryInternal {
    Page<Comment> search(String query, Pageable pageable);

    Page<Comment> search(Query query);

    @Async
    void index(Comment entity);

    @Async
    void deleteFromIndexById(Long id);
}

class CommentSearchRepositoryInternalImpl implements CommentSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final CommentRepository repository;

    CommentSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, CommentRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Comment> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Comment> search(Query query) {
        SearchHits<Comment> searchHits = elasticsearchTemplate.search(query, Comment.class);
        List<Comment> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Comment entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Comment.class);
    }
}
