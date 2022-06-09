package com.muryginds.searchEngine.entity;

import com.muryginds.searchEngine.parser.ParsingStatus;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "site")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Site {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  ParsingStatus status;

  @Column(name = "status_time")
  LocalDateTime statusTime;

  @Column(name = "last_error")
  String lastError;

  @Column(name = "url")
  String url;

  @Column(name = "name")
  String name;

//  @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE, mappedBy = "site", fetch = FetchType.LAZY)
//  List<Lemma> lemmas;
//  @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE, mappedBy = "site", fetch = FetchType.LAZY)
//  List<WebPage> pages;
//
//  public void removeLemma(Lemma lemma) {
//    lemmas.remove(lemma);
//  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Site site = (Site) o;

    if (url != null ? !url.equals(site.url) : site.url != null) {
      return false;
    }
    return name != null ? name.equals(site.name) : site.name == null;
  }

  @Override
  public int hashCode() {
    int result = url != null ? url.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }
}