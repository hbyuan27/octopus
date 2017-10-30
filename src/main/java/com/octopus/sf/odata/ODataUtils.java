package com.octopus.sf.odata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

public class ODataUtils {

  private enum QueryOption {
    SELECT("$select"), FILTER("$filter"), EXPAND("$expand"), FORMAT("$format"), ORDERBY("$orderby"), TOP("$top"), SKIP(
        "$skip");

    private final String value;

    private QueryOption(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public static String buildResourceUri(String entityTypeName, String valueOfBusinessKey) {
    StringBuilder sb = new StringBuilder(entityTypeName);
    if (valueOfBusinessKey != null) {
      sb.append("('").append(valueOfBusinessKey).append("')");
    }
    return sb.toString();
  }

  public static String buildResourceUri(String entityTypeName, LinkedHashMap<String, String> businessKeyMap) {
    StringBuilder sb = new StringBuilder(entityTypeName);
    if (businessKeyMap != null && businessKeyMap.size() > 0) {
      sb.append("(");
      for (Entry<String, String> businessKey : businessKeyMap.entrySet()) {
        sb.append(businessKey.getKey()).append("='").append(businessKey.getValue()).append("',");
      }
      sb.replace(sb.length() - 1, sb.length(), ")");
    }
    return sb.toString();
  }

  /**
   * Generate URI for querying OData Entry
   * 
   * @param queryOption
   * @return queryOptionUri
   */
  public static String buildQueryOptionUri(EntryQueryOption queryOption) {
    return (queryOption == null) ? "" : buildCommonQueryOption(queryOption).toString();
  }

  /**
   * Generate URI for querying OData Feed
   * 
   * @param queryOption
   * @return queryOptionUri
   */
  public static String buildQueryOptionUri(FeedQueryOption queryOption) {
    if (queryOption == null) {
      return "";
    }
    StringBuilder sb = buildCommonQueryOption(queryOption);
    // Top & Skip clause
    if (queryOption.getTop() != null) {
      appendPrefix(sb);
      sb.append(QueryOption.TOP).append("=");
      sb.append(queryOption.getTop());
    }
    if (queryOption.getSkip() != null) {
      appendPrefix(sb);
      sb.append(QueryOption.SKIP).append("=");
      sb.append(queryOption.getSkip());
    }
    // OrderBy clause
    Set<String> orderByOptions = queryOption.getOrderByOptions();
    if (orderByOptions != null) {
      processOptionList(sb, QueryOption.ORDERBY, orderByOptions);
      if (queryOption.isOrderByDesc()) {
        sb.append(" desc");
      }
    }
    // filter clause
    // TODO - need to support multiple filter options in the future
    // currently, for complex filter clause, you have to use FilterOption(String filterClause)
    FilterOption filterOption = queryOption.getFilterOption();
    if (filterOption != null) {
      appendPrefix(sb);
      sb.append(QueryOption.FILTER).append("=");
      if (filterOption.getFilterClause() != null) {
        sb.append(filterOption.getFilterClause());
      } else {
        sb.append(filterOption.getFilterablePropName());
        sb.append("%20").append(filterOption.getOperator()).append("%20");
        String filterValue = "";
        try {
          filterValue = URLEncoder.encode(filterOption.getFilterValue(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
          // will never happen since we're using StandardCharsets name
        }
        sb.append("'").append(filterValue).append("'");
      }
    }

    return sb.toString();
  }

  private static JSONObject toJSONObject(ODataEntity entity, OperationTypeEnum operationType) {
    JSONObject jo = new JSONObject();
    // add metadata property
    JSONObject metadata = new JSONObject();
    metadata.put("uri", entity.getEntityTypeName());
    jo.put("__metadata", metadata);
    if (operationType == OperationTypeEnum.UPSERT) {
      // add business keys
      for (Entry<String, String> businessKey : entity.getBusinessKeys().entrySet()) {
        jo.put(businessKey.getKey(), businessKey.getValue());
      }
    }
    // add properties
    Map<String, String> properties = entity.getProperties();
    if (properties != null) {
      for (Entry<String, String> property : properties.entrySet()) {
        jo.put(property.getKey(), property.getValue());
      }
    }
    // add navigations
    Map<String, ODataEntity> navigations = entity.getNavigations();
    if (navigations != null) {
      for (Entry<String, ODataEntity> navigation : navigations.entrySet()) {
        ODataEntity navEntity = navigation.getValue();
        jo.put(navigation.getKey(), toJSONObject(navEntity, operationType));
      }
    }
    return jo;
  }

  public static String buildJsonString(ODataEntity entity, OperationTypeEnum operationType) {
    return toJSONObject(entity, operationType).toJSONString();
  }

  private static StringBuilder buildCommonQueryOption(EntryQueryOption queryOption) {
    StringBuilder sb = new StringBuilder();
    // Select clause
    Set<String> selectOptions = queryOption.getSelectOptions();
    if (selectOptions != null) {
      processOptionList(sb, QueryOption.SELECT, selectOptions);
    }
    // Expand clause
    Set<String> expandOptions = queryOption.getExpandOptions();
    if (expandOptions != null) {
      processOptionList(sb, QueryOption.EXPAND, expandOptions);

    }
    // Format clause
    if (queryOption.getAcceptFormat() != null) {
      appendPrefix(sb);
      sb.append(QueryOption.FORMAT).append("=");
      sb.append(queryOption.getAcceptFormat());
    }
    return sb;
  }

  private static void processOptionList(StringBuilder sb, QueryOption queryOption, Set<String> options) {
    appendPrefix(sb);
    sb.append(queryOption).append("=");
    for (String option : options) {
      sb.append(option).append(",");
    }
    sb.delete(sb.length() - 1, sb.length());
  }

  private static void appendPrefix(StringBuilder sb) {
    if (sb.indexOf("?$") == -1) {
      sb.append("?");
    } else {
      sb.append("&");
    }
  }
}
