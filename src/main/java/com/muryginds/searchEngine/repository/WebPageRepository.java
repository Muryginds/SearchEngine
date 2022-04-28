package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.model.WebPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebPageRepository extends JpaRepository<WebPage, Integer> {
}