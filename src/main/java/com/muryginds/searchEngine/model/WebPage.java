package com.muryginds.searchEngine.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "page")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebPage {

    @Id
    @SequenceGenerator(name = "page_seq", sequenceName = "page_id_seq", allocationSize = 3)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    Integer id;

    @Column(name = "path")
    String path;

    @Column(name = "code")
    Integer code;

    @Column(name = "content")
    String content;
}