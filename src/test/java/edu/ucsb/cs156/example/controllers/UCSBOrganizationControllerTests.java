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
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
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

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganization/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/ucsbdiningcommons/post
  // (Perhaps should also have these for put and delete)

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_all_ucsborganizations_returns_correct() throws Exception {

    // arrange

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>();
    expectedOrgs.addAll(Arrays.asList(zpr));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrgs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id() throws Exception {
    // arrange

    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgCode("123")
            .orgTranslationShort("123")
            .orgTranslation("one two three")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("123"))).thenReturn(Optional.of(organization));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=123"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("123"));
    String expectedJson = mapper.writeValueAsString(organization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("WRONG"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=WRONG"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("WRONG"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id WRONG not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganization/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganization/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(zpr))).thenReturn(zpr);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post?orgCode=ZPR&orgTranslationShort=ZETA PHI RHO&orgTranslation=ZETA PHI RHO&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(zpr);
    String expectedJson = mapper.writeValueAsString(zpr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_organization() throws Exception {
    // arrange

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zpr));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=ZPR").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ZPR");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id ZPR deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("Awesome"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=Awesome").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("Awesome");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id Awesome not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization zprOrig =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(true)
            .build();

    UCSBOrganization zprEdited =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO EDIT")
            .orgTranslation("ZETA PHI RHO EDITED")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(zprEdited);

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zprOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgCode=ZPR")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ZPR");
    verify(ucsbOrganizationRepository, times(1))
        .save(zprEdited); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization editedOrganization =
        UCSBOrganization.builder()
            .orgCode("Amazing")
            .orgTranslationShort("ZETA PHI RHO EDIT")
            .orgTranslation("ZETA PHI RHO EDITED")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrganization);

    when(ucsbOrganizationRepository.findById(eq("Amazing"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgCode=Amazing")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("Amazing");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id Amazing not found", json.get("message"));
  }
}
