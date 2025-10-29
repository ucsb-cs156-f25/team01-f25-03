package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockBean private ArticleRepository articleRepository;
  @MockBean private UserRepository userRepository;

  // --- Authorization: GET /all ---
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk());
  }

  // --- Authorization: POST /post ---
  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/articles/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/articles/post")).andExpect(status().is(403));
  }

  // --- With mocks: GET /all ---
  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {
    Article a1 =
        Article.builder()
            .title("A1")
            .url("https://e.com/a1")
            .explanation("demo1")
            .email("u@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-27T13:45:00"))
            .build();

    Article a2 =
        Article.builder()
            .title("A2")
            .url("https://e.com/a2")
            .explanation("demo2")
            .email("u@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-27T14:00:00"))
            .build();

    when(articleRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(a1, a2)));

    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    verify(articleRepository, times(1)).findAll();
    assertEquals(
        mapper.writeValueAsString(Arrays.asList(a1, a2)),
        response.getResponse().getContentAsString());
  }

  // --- With mocks: POST /post ---
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_a_new_article_and_fields_are_set() throws Exception {

    Article returned =
        Article.builder()
            .title("Test Article")
            .url("https://example.com/a1")
            .explanation("demo")
            .email("you@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-27T13:45:00"))
            .build();
    when(articleRepository.save(any(Article.class))).thenReturn(returned);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "Test Article")
                    .param("url", "https://example.com/a1")
                    .param("explanation", "demo")
                    .param("email", "you@ucsb.edu")
                    .param("dateAdded", "2025-10-27T13:45:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository, times(1)).save(captor.capture());
    Article saved = captor.getValue();
    assertEquals("Test Article", saved.getTitle());
    assertEquals("https://example.com/a1", saved.getUrl());
    assertEquals("demo", saved.getExplanation());
    assertEquals("you@ucsb.edu", saved.getEmail());
    assertEquals(LocalDateTime.parse("2025-10-27T13:45:00"), saved.getDateAdded());

    assertEquals(mapper.writeValueAsString(returned), response.getResponse().getContentAsString());
  }

  // --- Authorization: GET by id ---
  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/articles").param("id", "7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id_if_exists() throws Exception {
    Article a =
        Article.builder()
            .title("A1")
            .url("https://e.com/a1")
            .explanation("demo")
            .email("u@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-27T13:45:00"))
            .build();

    when(articleRepository.findById(eq(7L))).thenReturn(Optional.of(a));

    MvcResult res =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(articleRepository, times(1)).findById(7L);
    assertEquals(mapper.writeValueAsString(a), res.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_get_404_if_not_found() throws Exception {
    when(articleRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult res =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articleRepository, times(1)).findById(7L);
    Map<String, Object> json = responseToJson(res);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Article with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {
    Article original =
        Article.builder()
            .title("Old Title")
            .url("https://old.com")
            .explanation("old")
            .email("old@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-29T13:45:00"))
            .build();
    when(articleRepository.findById(2L)).thenReturn(Optional.of(original));

    Article updated =
        Article.builder()
            .title("New Title")
            .url("https://new.com")
            .explanation("new explanation")
            .email("new@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-29T13:45:33"))
            .build();

    when(articleRepository.save(any(Article.class))).thenReturn(updated);

    String requestBody = mapper.writeValueAsString(updated);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles?id=2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository, times(1)).findById(2L);
    verify(articleRepository, times(1)).save(captor.capture());
    Article saved = captor.getValue();

    assertEquals("New Title", saved.getTitle());
    assertEquals("https://new.com", saved.getUrl());
    assertEquals("new explanation", saved.getExplanation());
    assertEquals("new@ucsb.edu", saved.getEmail());
    assertEquals(LocalDateTime.parse("2025-10-29T13:45:33"), saved.getDateAdded());

    assertEquals(mapper.writeValueAsString(updated), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_cannot_edit_nonexistent_article() throws Exception {
    when(articleRepository.findById(999L)).thenReturn(Optional.empty());

    Article incoming =
        Article.builder()
            .title("x")
            .url("https://x")
            .explanation("x")
            .email("x@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-29T00:00:00"))
            .build();

    mockMvc
        .perform(
            put("/api/articles?id=999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().isNotFound());

    verify(articleRepository, times(1)).findById(999L);
    verify(articleRepository, never()).save(any());
  }

  // --- Authorization: DELETE /api/articles ---
  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/articles?id=123").with(csrf())).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/articles?id=123").with(csrf())).andExpect(status().is(403));
  }

  // --- With mocks: DELETE /api/articles (success) ---
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_existing_article() throws Exception {
    Article existing =
        Article.builder()
            .id(2L)
            .title("t")
            .url("https://e.com")
            .explanation("x")
            .email("u@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2025-10-27T13:45:00"))
            .build();

    when(articleRepository.findById(2L)).thenReturn(Optional.of(existing));

    MvcResult response =
        mockMvc
            .perform(delete("/api/articles?id=2").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articleRepository, times(1)).findById(2L);
    verify(articleRepository, times(1)).delete(existing);

    assertEquals("record 2 deleted", response.getResponse().getContentAsString());
  }

  // --- With mocks: DELETE /api/articles (not found) ---
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_delete_nonexistent_article_returns_404() throws Exception {
    when(articleRepository.findById(123L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/articles?id=123").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articleRepository, times(1)).findById(123L);
    verify(articleRepository, never()).delete(any());

    assertEquals("record 123 not found", response.getResponse().getContentAsString());
  }
}
