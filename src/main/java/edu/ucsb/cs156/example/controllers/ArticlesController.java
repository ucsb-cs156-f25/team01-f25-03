package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {
  @Autowired private ArticleRepository articleRepository;

  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Article> allArticles() {
    return articleRepository.findAll();
  }

  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Article postArticle(
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "email") @RequestParam String email,
      @Parameter(name = "dateAdded", description = "ISO datetime, e.g. 2025-10-27T13:45:00")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateAdded) {
    Article a = new Article();
    a.setTitle(title);
    a.setUrl(url);
    a.setExplanation(explanation);
    a.setEmail(email);
    a.setDateAdded(dateAdded);

    return articleRepository.save(a);
  }

  @Operation(summary = "Get a single article")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Article getById(@Parameter(name = "id") @RequestParam Long id) {
    return articleRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(Article.class, id));
  }

  @Operation(summary = "Update a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Article updateArticleById(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody Article incoming) {
    Article article =
        articleRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

    article.setTitle(incoming.getTitle());
    article.setUrl(incoming.getUrl());
    article.setExplanation(incoming.getExplanation());
    article.setEmail(incoming.getEmail());
    article.setDateAdded(incoming.getDateAdded());

    return articleRepository.save(article);
  }

  @Operation(summary = "Delete a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public ResponseEntity<String> deleteArticleById(@Parameter(name = "id") @RequestParam Long id) {

    return articleRepository
        .findById(id)
        .map(
            existing -> {
              articleRepository.delete(existing);
              return ResponseEntity.ok("record " + id + " deleted");
            })
        .orElseGet(
            () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("record " + id + " not found"));
  }
}
