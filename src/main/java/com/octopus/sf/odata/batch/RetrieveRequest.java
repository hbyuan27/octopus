package com.octopus.sf.odata.batch;

import java.util.LinkedHashMap;

import com.octopus.sf.odata.EntryQueryOption;
import com.octopus.sf.odata.FeedQueryOption;
import com.octopus.sf.odata.ODataUtils;

public class RetrieveRequest implements BatchRequestPart {

  private final String entityTypeName;

  private final FeedQueryOption feedQueryOption;

  private final String valueOfBusinessKey;

  private final EntryQueryOption entryQueryOption;

  private final LinkedHashMap<String, String> businessKeyMap;

  /**
   * Retrieve Request represents a feed query
   * 
   * @param entityTypeName
   * @param queryOption
   */
  public RetrieveRequest(String entityTypeName, FeedQueryOption feedQueryOption) {
    this.entityTypeName = entityTypeName;
    this.feedQueryOption = feedQueryOption;
    this.valueOfBusinessKey = null;
    this.entryQueryOption = null;
    this.businessKeyMap = null;
  }

  /**
   * Retrieve Request represents a entry query with single business-key
   * 
   * @param entityTypeName
   * @param valueOfBusinessKey
   * @param entryQueryOption
   */
  public RetrieveRequest(String entityTypeName, String valueOfBusinessKey, EntryQueryOption entryQueryOption) {
    this.entityTypeName = entityTypeName;
    this.feedQueryOption = null;
    this.valueOfBusinessKey = valueOfBusinessKey;
    this.entryQueryOption = entryQueryOption;
    this.businessKeyMap = null;
  }

  /**
   * Retrieve Request represents a entry query with multiple business-key
   * 
   * @param entityTypeName
   * @param businessKeyMap
   * @param entryQueryOption
   */
  public RetrieveRequest(String entityTypeName, LinkedHashMap<String, String> businessKeyMap,
      EntryQueryOption entryQueryOption) {
    this.entityTypeName = entityTypeName;
    this.feedQueryOption = null;
    this.valueOfBusinessKey = null;
    this.entryQueryOption = entryQueryOption;
    this.businessKeyMap = businessKeyMap;
  }

  @Override
  public String getRawString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Content-Type: application/http").append("\n");
    sb.append("Content-Transfer-Encoding:binary").append("\n\n");
    sb.append("GET ").append(buildUri()).append(" HTTP/1.1").append("\n");
    sb.append("Accept: application/json").append("\n");
    return sb.toString();
  }

  private String buildUri() {
    String resourceUri = null;
    String queryOption = null;
    if (valueOfBusinessKey != null) {
      resourceUri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
      queryOption = ODataUtils.buildQueryOptionUri(entryQueryOption);
    } else if (businessKeyMap != null) {
      resourceUri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
      queryOption = ODataUtils.buildQueryOptionUri(entryQueryOption);
    } else {
      resourceUri = entityTypeName;
      queryOption = ODataUtils.buildQueryOptionUri(feedQueryOption);
    }
    return resourceUri + queryOption;
  }

}
