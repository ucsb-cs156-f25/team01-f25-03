package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
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

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockBean HelpRequestRepository helpRequestRepository;

  // Needed for security in tests (pattern from example controllers)
  @MockBean UserRepository userRepository;

  /* Authorization checks */

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/HelpRequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/HelpRequest/post")).andExpect(status().is(403));
  }

  /* Functional Test */

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_helprequests() throws Exception {
    LocalDateTime t1 = LocalDateTime.parse("2025-01-20T14:00:00");
    LocalDateTime t2 = LocalDateTime.parse("2025-01-20T14:10:00");

    HelpRequest h1 =
        HelpRequest.builder()
            .requesterEmail("test_h1@ucsb.edu")
            .teamId("f25-4pm-3")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(t1)
            .explanation("Need help with lab setup")
            .solved(false)
            .build();

    HelpRequest h2 =
        HelpRequest.builder()
            .requesterEmail("test_h2@ucsb.edu")
            .teamId("f25-4pm-2")
            .tableOrBreakoutRoom("Breakout 1")
            .requestTime(t2)
            .explanation("Question about Jpa04")
            .solved(true)
            .build();

    var expected = new ArrayList<HelpRequest>();
    expected.addAll(Arrays.asList(h1, h2));

    when(helpRequestRepository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_user_can_post_a_new_helprequest() throws Exception {
    LocalDateTime t = LocalDateTime.parse("2025-10-25T21:45:24");

    HelpRequest toSave =
        HelpRequest.builder()
            .requesterEmail("testEmail@ucsb.edu")
            .teamId("f25-4pm-3")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(t)
            .explanation("Oauth login error")
            .solved(true)
            .build();

    HelpRequest saved =
        HelpRequest.builder()
            .requesterEmail("testEmail@ucsb.edu")
            .teamId("f25-4pm-3")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(t)
            .explanation("Oauth login error")
            .solved(true)
            .build();

    when(helpRequestRepository.save(org.mockito.ArgumentMatchers.any(HelpRequest.class)))
        .thenReturn(saved);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/HelpRequest/post"
                        + "?requesterEmail=testEmail@ucsb.edu"
                        + "&teamId=f25-4pm-3"
                        + "&tableOrBreakoutRoom=Table 3"
                        + "&requestTime=2025-10-25T21:45:24"
                        + "&explanation=Oauth login error"
                        + "&solved=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    var captor = org.mockito.ArgumentCaptor.forClass(HelpRequest.class);
    verify(helpRequestRepository, times(1)).save(captor.capture());
    HelpRequest captured = captor.getValue();
    assertEquals(toSave, captured);
    String expectedJson = mapper.writeValueAsString(saved);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
    assertTrue(captured.getSolved());
  }
}
