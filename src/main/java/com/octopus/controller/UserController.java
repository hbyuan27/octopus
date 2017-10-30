package com.octopus.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.octopus.auth.Authentication;

@RestController
@RequestMapping(value = "/user")
public class UserController {

  @Autowired
  private Authentication auth;

  @GetMapping(value = "/login")
  public String login() {
    return auth.login();
  }

  @PostMapping(value = "/logout")
  public void logout(HttpServletRequest request) {
    auth.logout(request);
  }
}
