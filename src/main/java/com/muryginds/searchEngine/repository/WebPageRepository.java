package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WebPageRepository extends JpaRepository<WebPage, Integer> {
    Page<WebPage> findPageBySiteAndCode(Site site, Integer code, Pageable pageable);

    @Query(value = "SELECT page.* FROM"
        + " (SELECT page_id, CUME_DIST() OVER (ORDER BY SUM(\"rank\")) rel_rank from \"index\""
        + " WHERE lemma_id IN (SELECT id FROM lemma"
        + " WHERE lemma IN (:lemmas))"
        + " GROUP BY page_id"
        + " HAVING count(*) = :count) v"
        + " JOIN page ON v.page_id = page.id ORDER BY v.rel_rank DESC", nativeQuery = true)
    List<WebPage> findByLemmas(Collection<String> lemmas, int count);
}