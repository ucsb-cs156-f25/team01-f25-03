package edu.ucsb.cs156.example.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.services.CurrentUserService;
import edu.ucsb.cs156.example.services.wiremock.WiremockService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RecommendationRequestController.class)
public class RecommendationRequestControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockBean private RecommendationRequestRepository recommendationRequestRepository;

  @MockBean private WiremockService wiremockService;

  @MockBean private CurrentUserService currentUserService;

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
  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void test_postRecommendationRequest_creates_new_request_with_done_false_and_true()
      throws Exception {
    LocalDateTime dateRequested = LocalDateTime.parse("2024-10-10T10:00:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2024-11-01T12:00:00");

    for (boolean done : new boolean[] {false, true}) {
      RecommendationRequest saved = new RecommendationRequest();
      saved.setId(done ? 2L : 1L);
      saved.setRequesterEmail("alice@ucsb.edu");
      saved.setProfessorEmail("prof@ucsb.edu");
      saved.setExplanation("Grad school recommendation");
      saved.setDateRequested(dateRequested);
      saved.setDateNeeded(dateNeeded);
      saved.setDone(done);

      when(recommendationRequestRepository.save(any(RecommendationRequest.class)))
          .thenReturn(saved);

      mockMvc
          .perform(
              post("/api/recommendationrequest/post")
                  .param("requesterEmail", "alice@ucsb.edu")
                  .param("professorEmail", "prof@ucsb.edu")
                  .param("explanation", "Grad school recommendation")
                  .param("dateRequested", "2024-10-10T10:00:00")
                  .param("dateNeeded", "2024-11-01T12:00:00")
                  .param("done", String.valueOf(done))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.requesterEmail").value("alice@ucsb.edu"))
          .andExpect(jsonPath("$.professorEmail").value("prof@ucsb.edu"))
          .andExpect(jsonPath("$.explanation").value("Grad school recommendation"))
          .andExpect(jsonPath("$.dateRequested").value("2024-10-10T10:00:00"))
          .andExpect(jsonPath("$.dateNeeded").value("2024-11-01T12:00:00"))
          .andExpect(jsonPath("$.done").value(done));

      ArgumentCaptor<RecommendationRequest> captor =
          ArgumentCaptor.forClass(RecommendationRequest.class);
      verify(recommendationRequestRepository, times(1)).save(captor.capture());

      RecommendationRequest captured = captor.getValue();
      assertThat(captured.getRequesterEmail()).isEqualTo("alice@ucsb.edu");
      assertThat(captured.getProfessorEmail()).isEqualTo("prof@ucsb.edu");
      assertThat(captured.getExplanation()).isEqualTo("Grad school recommendation");
      assertThat(captured.getDateRequested()).isEqualTo(dateRequested);
      assertThat(captured.getDateNeeded()).isEqualTo(dateNeeded);
      assertThat(captured.getDone()).isEqualTo(done);

      reset(recommendationRequestRepository);
    }
  }
}
