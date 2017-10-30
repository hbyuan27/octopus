package com.octopus.auth;

import java.util.Map;

public interface SessionParams {

  String getUserName();

  Map<String, String> getCredential(String name);
}
