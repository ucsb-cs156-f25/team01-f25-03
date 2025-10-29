package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** The UCSBOrganizationsRepository is a repository for UCSBOrganization entities */
@Repository
public interface UCSBOrganizationRepository extends CrudRepository<UCSBOrganization, String> {}
