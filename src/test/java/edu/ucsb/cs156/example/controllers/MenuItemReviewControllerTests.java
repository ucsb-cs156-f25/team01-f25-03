package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
}
