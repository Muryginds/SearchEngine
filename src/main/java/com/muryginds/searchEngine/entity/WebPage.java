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
@Table(name = "page")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebPage {

    @Id
    @SequenceGenerator(name = "page_seq", sequenceName = "page_id_seq", allocationSize = 30)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    Integer id;

    @ManyToOne
    Site site;

    @Column(name = "path")
    String path;

    @Column(name = "code")
    Integer code;

    @Column(name = "content")
    String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WebPage webPage = (WebPage) o;

        if (site != null ? !site.equals(webPage.site) : webPage.site != null) {
            return false;
        }
        return path != null ? path.equals(webPage.path) : webPage.path == null;
    }

    @Override
    public int hashCode() {
        int result = site != null ? site.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}