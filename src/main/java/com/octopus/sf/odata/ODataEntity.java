package com.octopus.sf.odata;

import java.util.HashMap;
import java.util.Map;

public class ODataEntity {

  private final String entityTypeName;

  private final Map<String, String> businessKeys;

  private Map<String, String> properties;

  private Map<String, ODataEntity> navigations;

  public ODataEntity(String entityTypeName, Map<String, String> businessKeys) {
    this.entityTypeName = entityTypeName;
    this.businessKeys = businessKeys;
  }

  public ODataEntity(String entityTypeName) {
    this.entityTypeName = entityTypeName;
    this.businessKeys = null;
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public Map<String, String> getBusinessKeys() {
    return businessKeys;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public Map<String, ODataEntity> getNavigations() {
    return navigations;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void addProperty(String name, String value) {
    if (properties == null) {
      properties = new HashMap<String, String>();
    }
    properties.put(name, value);
  }
  
  public void addNavigation(String name, ODataEntity value) {
    if (navigations == null) {
      navigations = new HashMap<String, ODataEntity>();
    }
    navigations.put(name, value);
  }
}
