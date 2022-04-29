package com.muryginds.searchEngine.model;

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
    @SequenceGenerator(name = "page_seq", sequenceName = "page_id_seq", allocationSize = 3)
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

        if (id != null ? !id.equals(webPage.id) : webPage.id != null) {
            return false;
        }
        if (site != null ? !site.equals(webPage.site) : webPage.site != null) {
            return false;
        }
        if (path != null ? !path.equals(webPage.path) : webPage.path != null) {
            return false;
        }
        if (code != null ? !code.equals(webPage.code) : webPage.code != null) {
            return false;
        }
        return content != null ? content.equals(webPage.content) : webPage.content == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}