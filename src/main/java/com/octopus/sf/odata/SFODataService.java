package com.octopus.sf.odata;

import static com.octopus.sf.SFConstants.*;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.octopus.sf.SFAuthentication;
import com.octopus.sf.common.HttpParams;
import com.octopus.sf.common.WrappedHttpClient;
import com.octopus.sf.common.WrappedResponse;
import com.octopus.sf.odata.batch.BatchRequest;

@Service
public class SFODataService {

  // TODO start an additional thread to send $metadata request to SF which will accelerate the other OData requests.
  @Autowired
  private SFAuthentication sfAuth;

  @Autowired
  private WrappedHttpClient httpClient;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private static enum ResponseType {
    METADATA, ENTRY, FEED, BATCH, NONE;
  }

  /**
   * Get metadata for the given OData Entity
   * 
   * @param entityTypeName
   * @return response
   */
  public WrappedResponse getEntityMetadata(String entityTypeName) {
    String uri = entityTypeName.concat("/").concat(ODATA_ENDPOINT_METADATA);
    return execute(uri, HttpMethod.GET, null, null, ResponseType.METADATA);
  }

  /**
   * read an OData Entry into an ODataJsonBean wrapped by WrappedResponse
   * 
   * @param entityTypeName
   * @param valueOfBusinessKey
   * @param queryOption
   *          - include select, expand and format(accept content type)
   * @return response
   */
  public WrappedResponse readEntry(String entityTypeName, String valueOfBusinessKey, EntryQueryOption queryOption) {
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
    String queryOptionUri = ODataUtils.buildQueryOptionUri(queryOption);
    return execute(resourceUri + queryOptionUri, HttpMethod.GET, null, null, ResponseType.ENTRY);
  }

  /**
   * read an OData Entry into an ODataJsonBean wrapped by WrappedResponse
   * 
   * @param entityTypeName
   * @param businessKeyMap
   *          - <li>key: business-key name <li>value: value of the business-key
   * @param queryOption
   *          - include select, expand and format(accept content type)
   * @return response
   */
  public WrappedResponse readEntry(String entityTypeName, LinkedHashMap<String, String> businessKeyMap,
      EntryQueryOption queryOption) {
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
    String queryOptionUri = ODataUtils.buildQueryOptionUri(queryOption);
    return execute(resourceUri + queryOptionUri, HttpMethod.GET, null, null, ResponseType.ENTRY);
  }

  /**
   * read OData Feed into a set of ODataJsonBean wrapped by WrappedResponse
   * 
   * @param entityTypeName
   * @param queryOption
   * @return response
   */
  public WrappedResponse readFeed(String entityTypeName, FeedQueryOption queryOption) {
    String queryOptionUri = ODataUtils.buildQueryOptionUri(queryOption);
    return execute(entityTypeName + queryOptionUri, HttpMethod.GET, null, null, ResponseType.FEED);
  }

  /**
   * Create an Entry using data content converted from a JSON object
   * 
   * @param entityTypeName
   * @param entity
   * @return response
   */
  public WrappedResponse createEntry(ODataEntity entity) {
    String jsonString = ODataUtils.buildJsonString(entity, OperationTypeEnum.INSERT);
    return execute(entity.getEntityTypeName(), HttpMethod.POST, null, jsonString, ResponseType.ENTRY);
  }

  /**
   * Merge an existing Entry with a given EntityTypeName with single business-key<br>
   * This operation will only update the fields provided in JSON object
   * 
   * @param entityTypeName
   * @param valueOfBusinessKey
   * @param entity
   * @return response
   */
  public WrappedResponse mergeEntry(String entityTypeName, String valueOfBusinessKey, ODataEntity entity) {
    String jsonString = ODataUtils.buildJsonString(entity, OperationTypeEnum.MERGE);
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
    return execute(resourceUri, HttpMethod.POST, METHOD_TUNNEL_MERGE, jsonString, ResponseType.ENTRY);
  }

  /**
   * Merge an existing Entry with a given EntityTypeName with single business-key<br>
   * This operation will only update the fields provided in JSON object
   * 
   * @param entityTypeName
   * @param businessKeyMap
   *          - <li>key: business-key name <li>value: value of the business-key
   * @param entity
   * @return response
   */
  public WrappedResponse mergeEntry(String entityTypeName, LinkedHashMap<String, String> businessKeyMap,
      ODataEntity entity) {
    String jsonString = ODataUtils.buildJsonString(entity, OperationTypeEnum.MERGE);
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
    return execute(resourceUri, HttpMethod.POST, METHOD_TUNNEL_MERGE, jsonString, ResponseType.ENTRY);
  }

  /**
   * Replace an existing Entry with a given EntityTypeName with single business-key<br>
   * This operation will replace the entire Entry with new data
   * 
   * @param entityTypeName
   * @param valueOfBusinessKey
   * @param entity
   * @return response
   */
  public WrappedResponse replaceEntry(String entityTypeName, String valueOfBusinessKey, ODataEntity entity) {
    String jsonString = ODataUtils.buildJsonString(entity, OperationTypeEnum.REPLACE);
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
    return execute(resourceUri, HttpMethod.POST, METHOD_TUNNEL_PUT, jsonString, ResponseType.NONE);
  }

  /**
   * Replace an existing Entry with a given EntityTypeName with multiple business-keys<br>
   * This operation will replace the entire Entry with new data
   * 
   * @param entityTypeName
   * @param businessKeyMap
   *          - <li>key: business-key name <li>value: value of the business-key
   * @param entity
   * @return response
   */
  public WrappedResponse replaceEntry(String entityTypeName, LinkedHashMap<String, String> businessKeyMap,
      ODataEntity entity) {
    String jsonString = ODataUtils.buildJsonString(entity, OperationTypeEnum.REPLACE);
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
    return execute(resourceUri, HttpMethod.POST, METHOD_TUNNEL_PUT, jsonString, ResponseType.NONE);
  }

  /**
   * Delete an existing Entry with a given EntityTypeName with single business-key
   * 
   * @param entityTypeName
   * @param valueOfBusinessKey
   * @return response
   */
  public WrappedResponse deleteEntry(String entityTypeName, String valueOfBusinessKey) {
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
    return execute(resourceUri, HttpMethod.POST, METHOD_TUNNEL_DELETE, null, ResponseType.NONE);
  }

  /**
   * Delete an existing Entry with a given EntityTypeName with multiple business-keys
   * 
   * @param entityTypeName
   * @param businessKeyMap
   *          - <li>key: business-key name <li>value: value of the business-key
   * @return response
   */
  public WrappedResponse deleteEntry(String entityTypeName, LinkedHashMap<String, String> businessKeyMap) {
    String resourceUri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
    return execute(resourceUri, HttpMethod.POST, METHOD_TUNNEL_DELETE, null, ResponseType.NONE);
  }

  /**
   * Processing OData Batch Request
   * 
   * @param request
   * @return response
   */
  public WrappedResponse processBatchRequest(BatchRequest request) {
    // build batch request
    String absoluteUri = sfAuth.getServiceRootURI().concat(ODATA_ENDPOINT_BATCH);
    HttpParams params = new HttpParams(HttpMethod.POST);
    params.setContentType("multipart/mixed; boundary=" + request.getBoundary());
    params.setAuthorization(sfAuth.getAuthentication());
    params.setPayload(request.getPayload());
    // send batch request
    logger.info("OData batch operation starting...");
    long startTime = System.currentTimeMillis();
    WrappedResponse response = httpClient.execute(absoluteUri, params);
    long endTime = System.currentTimeMillis();
    logger.info("OData batch operation end, is response success: {}, time cost: {} ms",
        String.valueOf(response.isSuccess()), String.valueOf(endTime - startTime));
    return response;
  }

  /**
   * @param uri
   *          - the URI after Service-Root-URI(https://services-host/odata/v2/)
   * @param httpMethod
   * @param methodTunnel
   *          - in many scenarios clients are limited to the HTTP GET and POST methods only. With Method-Tunneling, a
   *          client sets up a request uses POST as the HTTP method instead of the actual required one. It then adds one
   *          more header, "X-HTTP-Method", and gives it the value MERGE, PUT or DELETE.
   * @param payload
   * @param responseType
   * @return response
   */
  private WrappedResponse execute(String uri, HttpMethod httpMethod, String methodTunnel, String payload,
      ResponseType responseType) {
    // prepare HTTP request
    String absoluteUri = sfAuth.getServiceRootURI().concat(uri);
    HttpParams params = new HttpParams(httpMethod);
    if (httpMethod == HttpMethod.GET) {
      if (ResponseType.METADATA.equals(responseType)) {
        params.setAcceptType(MediaType.APPLICATION_XML_VALUE);
      } else {
        params.setAcceptType(MediaType.APPLICATION_JSON_UTF8_VALUE);
      }
    }
    if (httpMethod == HttpMethod.POST) {
      params.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
      params.setAcceptType(MediaType.APPLICATION_JSON_UTF8_VALUE);
      params.setMethodTunnel(methodTunnel);
      params.setPayload(payload);
    }
    params.setAuthorization(sfAuth.getAuthentication());

    // send HTTP request
    logger.info("OData request starting, HttpMethod: {}, absolute uri: {}", httpMethod.toString(), absoluteUri);
    long startTime = System.currentTimeMillis();
    WrappedResponse response = httpClient.execute(absoluteUri, params);
    long endTime = System.currentTimeMillis();
    logger.info("OData request end, is response success: {}, time cost: {} ms", String.valueOf(response.isSuccess()),
        String.valueOf(endTime - startTime));

    // data conversion for the wrapped response
    if (response.isSuccess()) {
      // convert response data from string to JSONObject which represents an entry or a feed
      String jsonString = response.getContent();
      switch (responseType) {
      case ENTRY:
        response.setContent(parseEntry(jsonString));
        break;
      case FEED:
        response.setContent(parseFeed(jsonString));
        break;
      case METADATA:
        // TODO - parse xml
        break;
      default:
        break;
      }
    }

    return response;
  }

  private String parseEntry(String jsonString) {
    String result = null;
    JSONObject jo = JSONObject.parseObject(jsonString);
    if (jo != null) {
      // remove the outer braces of SF OData Entry string
      jo = jo.getJSONObject("d");
      if (jo != null) {
        result = jo.toJSONString();
      }
    }
    return result;
  }

  private String parseFeed(String jsonString) {
    String result = null;
    JSONObject jo = JSONObject.parseObject(jsonString);
    if (jo != null) {
      jo = jo.getJSONObject("d");
      if (jo != null) {
        JSONArray ja = jo.getJSONArray("results");
        if (ja != null) {
          result = ja.toJSONString();
        }
      }
    }
    return result;
  }

}
