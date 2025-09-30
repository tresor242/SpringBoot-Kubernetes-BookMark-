package com.tresorelila.bookmarker_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tresorelila.bookmarker_api.dto.BookmarkDTO;
import com.tresorelila.bookmarker_api.dto.BookmarksDTO;
import com.tresorelila.bookmarker_api.request.CreateBookmarkRequest;
import com.tresorelila.bookmarker_api.service.BookmarkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookmarkController.class)
class BookmarkControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BookmarkService bookmarkService;

    // --- Helpers pour fabriquer un BookmarksDTO cohérent ---
    private static BookmarksDTO pageDto(List<BookmarkDTO> items,
                                        long totalElements, int totalPages,
                                        int currentPage, boolean isFirst, boolean isLast,
                                        boolean hasNext, boolean hasPrevious) {
        return new BookmarksDTO(items, totalElements, totalPages,
                currentPage, isFirst, isLast, hasNext, hasPrevious);
    }

    @Test
    void getBookmarks_withoutQuery_callsGetWithDefaultPage1() throws Exception {
        var createdAt = Instant.parse("2025-01-01T12:00:00Z");
        var item = new BookmarkDTO(1L, "Title A", "https://a.example", createdAt);
        var dto = pageDto(List.of(item), 1, 1, 1, true, true, false, false);
        given(bookmarkService.getBookmarks(1)).willReturn(dto);

        mvc.perform(get("/api/bookmarks")) // page=1, query="" par défaut
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Title A"))
                .andExpect(jsonPath("$.data[0].url").value("https://a.example"))
                .andExpect(jsonPath("$.data[0].createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.isFirst").value(true))
                .andExpect(jsonPath("$.isLast").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        verify(bookmarkService).getBookmarks(1);
    }

    @Test
    void getBookmarks_withQuery_callsSearch() throws Exception {
        var item = new BookmarkDTO(2L, "Spring Tips", "https://b.example", Instant.parse("2025-02-02T00:00:00Z"));
        var dto = pageDto(List.of(item), 5, 3, 2, false, false, true, true);
        given(bookmarkService.searchBookmarks("java", 2)).willReturn(dto);

        mvc.perform(get("/api/bookmarks")
                        .param("page", "2")
                        .param("query", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Spring Tips"))
                .andExpect(jsonPath("$.currentPage").value(2));

        verify(bookmarkService).searchBookmarks("java", 2);
    }

    @Test
    void getBookmarks_withBlankQuery_fallsBackToGet() throws Exception {
        var dto = pageDto(List.of(), 0, 0, 3, false, false, false, true);
        given(bookmarkService.getBookmarks(3)).willReturn(dto);

        mvc.perform(get("/api/bookmarks")
                        .param("page", "3")
                        .param("query", "   ")) // espaces => blank
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(3));

        verify(bookmarkService).getBookmarks(3);
    }

    @Test
    void createBookmark_valid_returns201AndBody() throws Exception {
        var req = new CreateBookmarkRequest();
        req.setTitle("New");
        req.setUrl("https://ex.com");

        var created = new BookmarkDTO(10L, "New", "https://ex.com", Instant.parse("2025-03-03T10:10:10Z"));
        given(bookmarkService.createBookmark(any(CreateBookmarkRequest.class))).willReturn(created);

        mvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.url").value("https://ex.com"))
                .andExpect(jsonPath("$.createdAt").value("2025-03-03T10:10:10Z"));

        var captor = ArgumentCaptor.forClass(CreateBookmarkRequest.class);
        verify(bookmarkService).createBookmark(captor.capture());
    }

    @Test
    void createBookmark_invalid_returns400() throws Exception {
        // vide -> viole @NotEmpty sur title et url
        var invalidJson = """
            {"title":"", "url":""}
            """;

        mvc.perform(post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
