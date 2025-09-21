package com.tresorelila.bookmarker_api.service;

import com.tresorelila.bookmarker_api.dto.BookmarkDTO;
import com.tresorelila.bookmarker_api.dto.BookmarksDTO;
import com.tresorelila.bookmarker_api.entite.Bookmark;
import com.tresorelila.bookmarker_api.mapper.BookmarkMapper;
import com.tresorelila.bookmarker_api.repository.BookmarkRepository;
import com.tresorelila.bookmarker_api.repository.BookmarkVM;
import com.tresorelila.bookmarker_api.request.CreateBookmarkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository repository;
    private final BookmarkMapper bookmarkMapper;

    @Transactional(readOnly = true)
    public BookmarksDTO getBookmarks(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(pageNo, 10, Sort.Direction.DESC, "createdAt");

        Page<BookmarkVM> vmPage = repository.findAllVm(pageable);
        Page<BookmarkDTO> dtoPage = vmPage.map(vm ->
                new BookmarkDTO(vm.getId(), vm.getTitle(), vm.getUrl(), vm.getCreatedAt())
        );

        return new BookmarksDTO(dtoPage);
    }

    @Transactional(readOnly = true)
    public BookmarksDTO searchBookmarks(String query, Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(pageNo, 10, Sort.Direction.DESC, "createdAt");

        // Variante 1 : requête JPQL custom
        // Page<BookmarkVM> vmPage = repository.searchBookmarks(query, pageable);

        // Variante 2 : derived query (suffit pour un LIKE)
        Page<BookmarkVM> vmPage = repository.findByTitleContainsIgnoreCase(query, pageable);

        Page<BookmarkDTO> dtoPage = vmPage.map(vm ->
                new BookmarkDTO(vm.getId(), vm.getTitle(), vm.getUrl(), vm.getCreatedAt())
        );

        return new BookmarksDTO(dtoPage);
    }

    public BookmarkDTO createBookmark(CreateBookmarkRequest request) {
        Bookmark bookmark = new Bookmark(null, request.getTitle(), request.getUrl(), Instant.now());
        Bookmark savedBookmark = repository.save(bookmark);
        return bookmarkMapper.toDTO(savedBookmark);
    }
}
