package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Field;
import com.muryginds.searchEngine.repository.FieldRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldService {

  private final FieldRepository fieldRepository;

  public List<Field> findAll () {
    return fieldRepository.findAll();
  }
}