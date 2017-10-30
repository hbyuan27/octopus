package com.octopus.sf.odata.batch;

import static com.octopus.sf.SFConstants.*;

import java.util.LinkedHashMap;

import com.octopus.sf.odata.ODataEntity;
import com.octopus.sf.odata.ODataUtils;
import com.octopus.sf.odata.OperationTypeEnum;

public class ChangeRequest {

  private final OperationTypeEnum operationType;

  private final String entityTypeName;

  private final String valueOfBusinessKey;

  private final LinkedHashMap<String, String> businessKeyMap;

  private final ODataEntity entity;

  private ChangeRequest(ChangeRequestBuilder builder) {
    this.operationType = builder.operationType;
    this.entityTypeName = builder.entityTypeName;
    this.valueOfBusinessKey = builder.valueOfBusinessKey;
    this.businessKeyMap = builder.businessKeyMap;
    this.entity = builder.entity;
  }

  public String getRawString() {
    StringBuilder sb = new StringBuilder();
    sb.append("POST ").append(buildUri()).append(" HTTP/1.1").append("\n");
    sb.append("Content-Type: application/json").append("\n");
    switch (operationType) {
    case MERGE:
      sb.append("X-HTTP-Method: ").append(METHOD_TUNNEL_MERGE).append("\n");
      break;
    case REPLACE:
      sb.append("X-HTTP-Method: ").append(METHOD_TUNNEL_PUT).append("\n");
      break;
    case DELETE:
      sb.append("X-HTTP-Method: ").append(METHOD_TUNNEL_DELETE).append("\n");
      break;
    default:
      break;
    }
    sb.append("\n");
    if (entity != null) {
      String jsonBody = ODataUtils.buildJsonString(entity, operationType);
      if (jsonBody != null) {
        sb.append(jsonBody).append("\n");
      }
    }
    return sb.toString();
  }

  private String buildUri() {
    if (operationType == OperationTypeEnum.UPSERT) {
      return "upsert";
    }
    String resourceUri = null;
    if (valueOfBusinessKey != null) {
      resourceUri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
    } else if (businessKeyMap != null) {
      resourceUri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
    } else {
      // for insert case and feed query
      resourceUri = entityTypeName;
    }
    return resourceUri;
  }

  public static class ChangeRequestBuilder {

    private OperationTypeEnum operationType;

    private String entityTypeName = null;

    private String valueOfBusinessKey = null;

    private LinkedHashMap<String, String> businessKeyMap = null;

    private ODataEntity entity = null;

    public ChangeRequest buildInsertRequest(ODataEntity entity) {
      this.entityTypeName = entity.getEntityTypeName();
      this.operationType = OperationTypeEnum.INSERT;
      this.entity = entity;
      return new ChangeRequest(this);
    }

    /**
     * For some SF OData EntityTypes, if you cannot successfully process your batch with Update operations, please try
     * with UPSERT.<br>
     * You must provide EntityType name in "__metadata" property and use "upsert" instead of the normal service URI.
     * 
     * @param entity
     * @return
     */
    public ChangeRequest buildUpsertRequest(ODataEntity entity) {
      this.entityTypeName = entity.getEntityTypeName();
      this.operationType = OperationTypeEnum.UPSERT;
      this.entity = entity;
      return new ChangeRequest(this);
    }

    public ChangeRequest buildDeleteRequest(String entityTypeName, String valueOfBusinessKey) {
      this.operationType = OperationTypeEnum.DELETE;
      this.entityTypeName = entityTypeName;
      this.valueOfBusinessKey = valueOfBusinessKey;
      return new ChangeRequest(this);
    }

    public ChangeRequest buildDeleteRequest(String entityTypeName, LinkedHashMap<String, String> businessKeyMap) {
      this.operationType = OperationTypeEnum.DELETE;
      this.entityTypeName = entityTypeName;
      this.businessKeyMap = businessKeyMap;
      return new ChangeRequest(this);
    }

    public ChangeRequest buildMergeRequest(String entityTypeName, String valueOfBusinessKey, ODataEntity entity) {
      this.operationType = OperationTypeEnum.MERGE;
      this.entityTypeName = entityTypeName;
      this.valueOfBusinessKey = valueOfBusinessKey;
      this.entity = entity;
      return new ChangeRequest(this);
    }

    public ChangeRequest buildMergeRequest(String entityTypeName, LinkedHashMap<String, String> businessKeyMap,
        ODataEntity entity) {
      this.operationType = OperationTypeEnum.MERGE;
      this.entityTypeName = entityTypeName;
      this.businessKeyMap = businessKeyMap;
      this.entity = entity;
      return new ChangeRequest(this);
    }

    public ChangeRequest buildReplaceRequest(String entityTypeName, String valueOfBusinessKey, ODataEntity entity) {
      this.operationType = OperationTypeEnum.REPLACE;
      this.entityTypeName = entityTypeName;
      this.valueOfBusinessKey = valueOfBusinessKey;
      this.entity = entity;
      return new ChangeRequest(this);
    }

    public ChangeRequest buildReplaceRequest(String entityTypeName, LinkedHashMap<String, String> businessKeyMap,
        ODataEntity entity) {
      this.operationType = OperationTypeEnum.REPLACE;
      this.entityTypeName = entityTypeName;
      this.businessKeyMap = businessKeyMap;
      this.entity = entity;
      return new ChangeRequest(this);
    }

  }
}
