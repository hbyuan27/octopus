package com.octopus.auth;

import javax.servlet.http.HttpServletRequest;

public interface Authentication {

  String login();

  void logout(HttpServletRequest request);

}
