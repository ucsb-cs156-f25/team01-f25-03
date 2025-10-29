package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for RecommendationRequest */
@Tag(name = "RecommendationRequest")
@RequestMapping("/api/recommendationrequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * Get all recommendation requests
   *
   * @return all RecommendationRequest entries as JSON
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequest() {
    Iterable<RecommendationRequest> requests = recommendationRequestRepository.findAll();
    return requests;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail email of the requester
   * @param professorEmail email of the professor
   * @param explanation explanation text
   * @param dateRequested when it was requested
   * @param dateNeeded when it is needed
   * @param done boolean status
   * @return the saved RecommendationRequest object
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "dateRequested")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(name = "dateNeeded")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    log.info("Creating RecommendationRequest for requesterEmail={}", requesterEmail);

    RecommendationRequest request = new RecommendationRequest();
    request.setRequesterEmail(requesterEmail);
    request.setProfessorEmail(professorEmail);
    request.setExplanation(explanation);
    request.setDateRequested(dateRequested);
    request.setDateNeeded(dateNeeded);
    request.setDone(done);

    RecommendationRequest savedRequest = recommendationRequestRepository.save(request);
    return savedRequest;
  }

  /**
   * Get a single recommendation request by id
   *
   * @param id the id of the recommendation request
   * @return the matching RecommendationRequest
   */
  @Operation(summary = "Get a single recommendation request by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getRecommendationRequestById(
      @Parameter(name = "id") @RequestParam Long id) {

    return recommendationRequestRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));
  }
}
