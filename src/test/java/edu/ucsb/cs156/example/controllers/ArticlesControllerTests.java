package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
}
