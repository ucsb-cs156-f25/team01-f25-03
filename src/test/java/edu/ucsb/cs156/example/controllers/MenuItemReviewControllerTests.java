package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {
  @MockBean MenuItemReviewRepository menuItemReviewRepository;
  @MockBean UserRepository userRepository;

  // Tests w/ Mocks for database actions

  @WithMockUser(roles = {})
  @Test
  public void test_cant_get_all_without_login() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_login_user_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {})
  @Test
  public void test_cant_post_without_login() throws Exception {
    mockMvc.perform(post("/api/menuitemreview/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {})
  @Test
  public void test_cant_get_single_without_login() throws Exception {
    mockMvc.perform(get("/api/menuitemreview?id=1")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {})
  @Test
  public void test_cant_put_without_login() throws Exception {
    mockMvc
        .perform(
            put(
                "/api/menuitemreview?itemId=1&reviewerEmail=abc@xyz.com&stars=1"
                    + "dateReviewed=2025-10-28T00:00:00&comments=terrible"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_cant_put_as_user() throws Exception {
    mockMvc
        .perform(
            put(
                "/api/menuitemreview?itemId=1&reviewerEmail=abc@xyz.com&stars=1"
                    + "dateReviewed=2025-10-28T00:00:00&comments=terrible"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {})
  @Test
  public void test_cant_delete_without_login() throws Exception {
    mockMvc.perform(delete("/api/menuitemreview?id=1")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_cant_delete_as_user() throws Exception {
    mockMvc.perform(delete("/api/menuitemreview?id=1")).andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menuitemreviews() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-10-28T00:00:00");
    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(Long.valueOf(1))
            .reviewerEmail("johnsmith@gmail.com")
            .stars(3)
            .dateReviewed(ldt1)
            .comments("mid")
            .build();

    LocalDateTime ldt2 = LocalDateTime.parse("2025-10-28T12:00:00");
    MenuItemReview menuItemReview2 =
        MenuItemReview.builder()
            .itemId(Long.valueOf(2))
            .reviewerEmail("janedoe@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt2)
            .comments("tasty")
            .build();

    ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
    expectedReviews.addAll(Arrays.asList(menuItemReview1, menuItemReview2));

    when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedReviews);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void user_can_post_a_new_menuitemreview() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-10-28T00:00:00");
    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(Long.valueOf(1))
            .reviewerEmail("johnsmith@gmail.com")
            .stars(3)
            .dateReviewed(ldt1)
            .comments("mid")
            .build();

    when(menuItemReviewRepository.save(eq(menuItemReview1))).thenReturn(menuItemReview1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post?itemId=1&reviewerEmail=johnsmith@gmail.com&stars=3"
                        + "&dateReviewed=2025-10-28T00:00:00&comments=mid")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).save(menuItemReview1);
    String expectedJson = mapper.writeValueAsString(menuItemReview1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void user_can_get_single_when_exists() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-10-28T00:00:00");
    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(Long.valueOf(1))
            .reviewerEmail("johnsmith@gmail.com")
            .stars(3)
            .dateReviewed(ldt1)
            .comments("mid")
            .build();

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(menuItemReview1));

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview?id=7")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(menuItemReview1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_user_cant_get_single_when_does_not_exist() throws Exception {
    // arrange
    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreview?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReview with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_existing_menuitemreview() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-10-28T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-10-29T00:00:00");

    MenuItemReview menuItemReviewOrig =
        MenuItemReview.builder()
            .itemId(Long.valueOf(1))
            .reviewerEmail("johnsmith@gmail.com")
            .stars(3)
            .dateReviewed(ldt1)
            .comments("mid")
            .build();

    MenuItemReview menuItemReviewEdited =
        MenuItemReview.builder()
            .itemId(Long.valueOf(2))
            .reviewerEmail("jackdoe@yahoo.com")
            .stars(5)
            .dateReviewed(ldt2)
            .comments("amazing")
            .build();

    String requestBody = mapper.writeValueAsString(menuItemReviewEdited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(menuItemReviewOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(67L);
    verify(menuItemReviewRepository, times(1))
        .save(menuItemReviewEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-10-28T00:00:00");

    MenuItemReview menuItemReviewEdited =
        MenuItemReview.builder()
            .itemId(Long.valueOf(1))
            .reviewerEmail("johnsmith@gmail.com")
            .stars(3)
            .dateReviewed(ldt1)
            .comments("mid")
            .build();

    String requestBody = mapper.writeValueAsString(menuItemReviewEdited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_review() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-10-28T00:00:00");
    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(Long.valueOf(1))
            .reviewerEmail("johnsmith@gmail.com")
            .stars(3)
            .dateReviewed(ldt1)
            .comments("mid")
            .build();

    when(menuItemReviewRepository.findById(eq(15L))).thenReturn(Optional.of(menuItemReview1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/menuitemreview?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(15L);
    verify(menuItemReviewRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_review_that_does_not_exist() throws Exception {
    when(menuItemReviewRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/menuitemreview?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 15 not found", json.get("message"));
  }
}
