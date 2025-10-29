package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* This is a REST controller for HelpRequest */
@Tag(name = "HelpRequest")
@RequestMapping("/api/HelpRequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /*
   * List all help requesty
   *
   * @return an iterable of help request
   */
  @Operation(summary = "List all help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    Iterable<HelpRequest> helpRequest = helpRequestRepository.findAll();
    return helpRequest;
  }

  /*
   * Create a new help request
   * String requesterEmail
   * String teamId
   * String tableOrBreakoutRoom
   * LocalDateTime requestTime
   * String explanation
   * boolean solved
   *
   * @return an iterable of help request
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(
              name = "requestTime",
              description =
                  "in iso format - YYYY-MM-DDTHH:MM:SS, see in https://en.wikipedia.org/wiki/ISO_8601, e.g. 2007-03-01T13:00:00")
          @RequestParam("requestTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime reqDateTime,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved)
      throws JsonProcessingException {
    HelpRequest helpRequest = new HelpRequest();
    helpRequest.setRequesterEmail(requesterEmail);
    helpRequest.setTeamId(teamId);
    helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    helpRequest.setRequestTime(reqDateTime);
    helpRequest.setExplanation(explanation);
    helpRequest.setSolved(solved);
    HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);
    return savedHelpRequest;
  }

  /**
   * Get a single HelpRequest by id
   *
   * @param id the id of the HelpRequest
   * @return a HelpRequest if found
   */
  @Operation(summary = "Get a single help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {

    return helpRequestRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
  }

  @Operation(summary = "Update a single help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public HelpRequest updateHelpRequest(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid HelpRequest incoming) {

    HelpRequest existing =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    existing.setRequesterEmail(incoming.getRequesterEmail());
    existing.setTeamId(incoming.getTeamId());
    existing.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
    existing.setRequestTime(incoming.getRequestTime());
    existing.setExplanation(incoming.getExplanation());
    existing.setSolved(incoming.getSolved());

    helpRequestRepository.save(existing);
    return existing;
  }

  @Operation(summary = "Delete a single help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Map<String, String> deleteHelpRequest(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest existing =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    helpRequestRepository.delete(existing);

    Map<String, String> response = new HashMap<>();
    response.put("message", String.format("HelpRequest with id %d deleted", id));
    return response;
  }
}
