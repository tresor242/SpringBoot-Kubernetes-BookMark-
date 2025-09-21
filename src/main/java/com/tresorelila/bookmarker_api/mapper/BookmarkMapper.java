package com.tresorelila.bookmarker_api.mapper;

import com.tresorelila.bookmarker_api.dto.BookmarkDTO;
import com.tresorelila.bookmarker_api.entite.Bookmark;
import org.springframework.stereotype.Component;

@Component
public class BookmarkMapper {

    public BookmarkDTO toDTO(Bookmark bookmark) {
        return new BookmarkDTO(
                bookmark.getId(),
                bookmark.getTitle(),
                bookmark.getUrl(),
                bookmark.getCreatedAt()
        );
    }
}