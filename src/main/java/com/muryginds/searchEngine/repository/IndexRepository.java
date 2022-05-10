package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.entity.Index;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexRepository extends JpaRepository<Index, Integer> {
}