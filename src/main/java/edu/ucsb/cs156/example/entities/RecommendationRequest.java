package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_request")
public class RecommendationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requesterEmail;
    private String professorEmail;
    private String explanation;
    private LocalDateTime dateRequested;
    private LocalDateTime dateNeeded;
    private boolean done;
}
