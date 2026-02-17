package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.AuthenticationRequest;
import com.tashicell.sop.Record.AuthenticationResponse;
import com.tashicell.sop.Service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/sop/api/auth")//rename it later
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationService service;

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) throws Exception {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response ) throws IOException {
    service.refreshToken(request, response);
  }
  
}
