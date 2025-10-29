package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommons */
@Tag(name = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * THis method returns a list of all ucsborganizations.
   *
   * @return a list of all ucsborganizations
   */
  @Operation(summary = "List all ucsb organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allOrganizations() {
    Iterable<UCSBOrganization> organizations = ucsbOrganizationRepository.findAll();
    return organizations;
  }

  /**
   * This method returns a single organization.
   *
   * @param orgCode code of the organization
   * @return a single organization
   */
  @Operation(summary = "Get a single organization")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(@Parameter(name = "code") @RequestParam String orgCode) {
    UCSBOrganization organization =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    return organization;
  }

  /**
   * This method creates a new organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @param orgTranslationShort shortened org translation
   * @param orgTranslation org translation
   * @param inactive if the org is inactive
   * @return the save organization
   */
  @Operation(summary = "Create a new organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {

    UCSBOrganization organization = new UCSBOrganization();
    organization.setOrgCode(orgCode);
    organization.setOrgTranslationShort(orgTranslationShort);
    organization.setOrgTranslation(orgTranslation);
    organization.setInactive(inactive);

    UCSBOrganization savedOrganization = ucsbOrganizationRepository.save(organization);

    return savedOrganization;
  }
}
