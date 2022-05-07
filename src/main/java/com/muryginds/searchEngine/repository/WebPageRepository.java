package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebPageRepository extends JpaRepository<WebPage, Integer> {
    Page<WebPage> findPageBySiteAndCode(Site site, Integer code, Pageable pageable);

    int countBySite(Site site);

}