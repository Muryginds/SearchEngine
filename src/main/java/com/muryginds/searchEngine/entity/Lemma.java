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
@Table(name = "lemma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lemma {

  @Id
  @SequenceGenerator(name = "lemma_seq", sequenceName = "lemma_id_seq", allocationSize = 30)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lemma_seq")
  Integer id;

  @ManyToOne
  Site site;

  @Column(name = "lemma")
  String lemma;

  @Column(name = "frequency")
  Integer frequency;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Lemma lemma1 = (Lemma) o;

    if (site != null ? !site.equals(lemma1.site) : lemma1.site != null) {
      return false;
    }
    return lemma != null ? lemma.equals(lemma1.lemma) : lemma1.lemma == null;
  }

  @Override
  public int hashCode() {
    int result = site != null ? site.hashCode() : 0;
    result = 31 * result + (lemma != null ? lemma.hashCode() : 0);
    return result;
  }
}