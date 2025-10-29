package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {
  @MockBean private RecommendationRequestRepository recommendationRequestRepository;
  @MockBean UserRepository userRepository;

  // === /all endpoint ===
  @Test
  @WithMockUser(roles = {"USER"})
  public void test_allRecommendationRequest_returns_all_requests() throws Exception {
    RecommendationRequest r1 = new RecommendationRequest();
    r1.setId(1L);
    r1.setRequesterEmail("student1@ucsb.edu");

    RecommendationRequest r2 = new RecommendationRequest();
    r2.setId(2L);
    r2.setRequesterEmail("student2@ucsb.edu");

    when(recommendationRequestRepository.findAll()).thenReturn(List.of(r1, r2));

    mockMvc
        .perform(get("/api/recommendationrequest/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].requesterEmail").value("student1@ucsb.edu"))
        .andExpect(jsonPath("$[1].requesterEmail").value("student2@ucsb.edu"));

    verify(recommendationRequestRepository).findAll();
  }

  // === /post endpoint ===
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-01-10T00:00:00");

    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("PhD recommendation")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(recommendationRequest)))
        .thenReturn(recommendationRequest);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequest/post?requesterEmail=student1@ucsb.edu&professorEmail=prof@ucsb.edu&explanation=PhD recommendation&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-01-10T00:00:00&done=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    when(recommendationRequestRepository.save(eq(recommendationRequest)))
        .thenReturn(recommendationRequest);

    // assert
    verify(recommendationRequestRepository, times(1)).save(recommendationRequest);
    String expectedJson = mapper.writeValueAsString(recommendationRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // === / GET single record ===
  @Test
  @WithMockUser(roles = {"USER"})
  public void test_getRecommendationRequestById_returns_record_when_exists() throws Exception {
    RecommendationRequest request = new RecommendationRequest();
    request.setId(5L);
    request.setRequesterEmail("bob@ucsb.edu");
    request.setProfessorEmail("prof@ucsb.edu");
    request.setExplanation("PhD recommendation");

    when(recommendationRequestRepository.findById(5L)).thenReturn(java.util.Optional.of(request));

    mockMvc
        .perform(get("/api/recommendationrequest").param("id", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.requesterEmail").value("bob@ucsb.edu"))
        .andExpect(jsonPath("$.professorEmail").value("prof@ucsb.edu"))
        .andExpect(jsonPath("$.explanation").value("PhD recommendation"));

    verify(recommendationRequestRepository, times(1)).findById(5L);
  }

  @Test
  @WithMockUser(roles = {"USER"})
  public void test_getRecommendationRequestById_throws_when_not_found() throws Exception {
    when(recommendationRequestRepository.findById(999L)).thenReturn(java.util.Optional.empty());

    mockMvc
        .perform(get("/api/recommendationrequest").param("id", "999"))
        .andExpect(status().isNotFound());

    verify(recommendationRequestRepository, times(1)).findById(999L);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendation_request() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");
    LocalDateTime ldt3 = LocalDateTime.parse("2024-01-03T00:00:00");
    LocalDateTime ldt4 = LocalDateTime.parse("2025-01-03T00:00:00");

    RecommendationRequest recommendationRequestOrig =
        RecommendationRequest.builder()
            .requesterEmail("abc@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Grad school recommendation")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    RecommendationRequest recommendationRequestEdited =
        RecommendationRequest.builder()
            .requesterEmail("edf@ucsb.edu")
            .professorEmail("lol@ucsb.edu")
            .explanation("whatever")
            .dateRequested(ldt3)
            .dateNeeded(ldt4)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

    when(recommendationRequestRepository.findById(eq(67L)))
        .thenReturn(Optional.of(recommendationRequestOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1))
        .save(recommendationRequestEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendation_request_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    RecommendationRequest recommendationRequestEdited =
        RecommendationRequest.builder()
            .requesterEmail("abc@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Grad school recommendation")
            .dateRequested(ldt1)
            .dateNeeded(ldt1)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void
      admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
          throws Exception {
    // arrange

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequest?id=67").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_existing_recommendationrequest() throws Exception {
    // arrange
    RecommendationRequest existing =
        RecommendationRequest.builder()
            .requesterEmail("test@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("explanation")
            .dateRequested(LocalDateTime.parse("2024-01-01T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2024-02-01T00:00:00"))
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(existing));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequest?id=67").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1)).delete(existing);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 deleted", json.get("message"));
  }
}
