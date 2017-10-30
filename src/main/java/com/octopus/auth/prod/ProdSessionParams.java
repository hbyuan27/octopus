package com.octopus.auth.prod;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.octopus.Profiles;
import com.octopus.auth.SessionParams;

@Component
@Profile(value = Profiles.PROD)
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProdSessionParams implements SessionParams {

  @Override
  public String getUserName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getCredential(String name) {
    // TODO Auto-generated method stub
    return null;
  }

}