package com.fileshareappv1.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.fileshareappv1.myapp.domain.File;
import com.fileshareappv1.myapp.repository.FileRepository;
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
 * Spring Data Elasticsearch repository for the {@link File} entity.
 */
public interface FileSearchRepository extends ElasticsearchRepository<File, Long>, FileSearchRepositoryInternal {}

interface FileSearchRepositoryInternal {
    Page<File> search(String query, Pageable pageable);

    Page<File> search(Query query);

    @Async
    void index(File entity);

    @Async
    void deleteFromIndexById(Long id);
}

class FileSearchRepositoryInternalImpl implements FileSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final FileRepository repository;

    FileSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, FileRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<File> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<File> search(Query query) {
        SearchHits<File> searchHits = elasticsearchTemplate.search(query, File.class);
        List<File> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(File entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), File.class);
    }
}
