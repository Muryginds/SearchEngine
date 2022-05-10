package com.muryginds.searchEngine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "index")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Index {

  @Id
  @SequenceGenerator(name = "index_seq", sequenceName = "index_id_seq", allocationSize = 30)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "index_seq")
  Integer id;

  @ManyToOne
  WebPage page;

  @ManyToOne
  Lemma lemma;

  @Column(name = "rank")
  Double rank;
}