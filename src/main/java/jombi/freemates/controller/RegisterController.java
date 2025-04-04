package jombi.freemates.controller;

import jombi.freemates.model.dto.RegisterRequest;
import jombi.freemates.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegisterController {

  private final RegisterService registerService;

  @PostMapping("/api/auth/register")
  public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
    return ResponseEntity.ok(registerService.register(request));
  }
}
