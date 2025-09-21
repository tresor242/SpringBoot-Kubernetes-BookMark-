package com.tresorelila.bookmarker_api.repository;

import com.tresorelila.bookmarker_api.entite.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
        select b.id as id, b.title as title, b.url as url, b.createdAt as createdAt
        from Bookmark b
        """)
    Page<BookmarkVM> findAllVm(Pageable pageable);

    @Query("""
        select b.id as id, b.title as title, b.url as url, b.createdAt as createdAt
        from Bookmark b
        where lower(b.title) like lower(concat('%', :query, '%'))
        """)
    Page<BookmarkVM> searchBookmarks(@Param("query") String query, Pageable pageable);


    Page<BookmarkVM> findByTitleContainsIgnoreCase(String query, Pageable pageable);
}
