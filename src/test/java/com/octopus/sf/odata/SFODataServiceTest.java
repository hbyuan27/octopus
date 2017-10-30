package com.octopus.sf.odata;

import static com.octopus.sf.SFConstants.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.octopus.AbstractUnitTest;
import com.octopus.sf.SFAuthentication;
import com.octopus.sf.common.HttpParams;
import com.octopus.sf.common.WrappedHttpClient;
import com.octopus.sf.common.WrappedResponse;
import com.octopus.sf.odata.EntryQueryOption;
import com.octopus.sf.odata.FeedQueryOption;
import com.octopus.sf.odata.FilterOption;
import com.octopus.sf.odata.ODataEntity;
import com.octopus.sf.odata.ODataUtils;
import com.octopus.sf.odata.OperationTypeEnum;
import com.octopus.sf.odata.QueryFilterEnum;
import com.octopus.sf.odata.SFODataService;
import com.octopus.sf.odata.batch.BatchRequest;
import com.octopus.sf.odata.batch.BatchRequestPart;
import com.octopus.sf.odata.batch.ChangeSet;
import com.octopus.sf.odata.batch.RetrieveRequest;
import com.octopus.sf.odata.batch.ChangeRequest.ChangeRequestBuilder;

public class SFODataServiceTest extends AbstractUnitTest {

  private SFODataService testObject;

  @Mock
  private SFAuthentication sfAuth;

  @Mock
  private WrappedHttpClient httpClient;

  @Before
  public void setUp() {
    testObject = new SFODataService();
    ReflectionTestUtils.setField(testObject, "sfAuth", sfAuth);
    ReflectionTestUtils.setField(testObject, "httpClient", httpClient);
    // mock SF OData Authentication component
    when(sfAuth.getAuthentication()).thenReturn("TestSFAuthString");
    when(sfAuth.getServiceRootURI()).thenReturn("https://testhost/odata/v2/");
  }

  @Test
  public void testCreateEntrySuccess() {
    // mock an entity to be created via ODataService
    ODataEntity entity = new ODataEntity("TestEntityType");
    entity.addProperty("prop1", "pv1");
    entity.addProperty("prop2", "pv2");
    ODataEntity nav1 = new ODataEntity("TestNavType1");
    nav1.addProperty("navProp1", "npv1");
    ODataEntity nav2 = new ODataEntity("TestNavType2");
    nav2.addProperty("navProp2", "npv2");
    entity.addNavigation("nav1", nav1);
    entity.addNavigation("nav2", nav2);
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    String createdEntityJSON = "{\"d\": {\"prop1\": \"pv1\",\"prop2\": \"pv2\",\"nav1\": {\"navProp1\": \"npv1\"},\"nav2\": {\"navProp2\": \"npv2\"}}}";
    httpResponse.setContent(createdEntityJSON);
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq("https://testhost/odata/v2/TestEntityType"), argumentCaptor.capture())).thenReturn(
        httpResponse);
    // run test method
    WrappedResponse result = testObject.createEntry(entity);
    // verify result
    JSONObject jo = JSONObject.parseObject(result.getContent());
    Assert.assertNotNull(jo);
    Assert.assertEquals("pv1", jo.getString("prop1"));
    Assert.assertEquals("pv2", jo.getString("prop2"));
    Assert.assertEquals("npv1", jo.getJSONObject("nav1").getString("navProp1"));
    Assert.assertEquals("npv2", jo.getJSONObject("nav2").getString("navProp2"));
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.POST, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getContentType());
    Assert.assertNull(argumentCaptor.getValue().getMethodTunnel());
    Assert.assertEquals(ODataUtils.buildJsonString(entity, OperationTypeEnum.INSERT), argumentCaptor.getValue()
        .getPayload());
  }

  @Test
  public void testReadFeed() {
    // mock query option
    FeedQueryOption queryOption = new FeedQueryOption();
    queryOption.addExpandOption("e1");
    queryOption.addExpandOption("e2");
    queryOption.addOrderByOption("o1");
    queryOption.addSelectOption("s1");
    queryOption.addSelectOption("s2");
    FilterOption fo = new FilterOption("f1", QueryFilterEnum.EQUAL, "fv1");
    queryOption.setFilterOption(fo);
    String queryString = "https://testhost/odata/v2/TestEntityType?$select=s1,s2&$expand=e1,e2&$orderby=o1&$filter=f1%20eq%20'fv1'";
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    String createdEntityJSON = "{\"d\": {\"results\": [{\"prop1\": \"pv1\",\"prop2\": \"pv2\",\"nav1\": {\"navProp1\": \"npv1\"}}]}}";
    httpResponse.setContent(createdEntityJSON);
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq(queryString), argumentCaptor.capture())).thenReturn(httpResponse);
    // run test method
    WrappedResponse result = testObject.readFeed("TestEntityType", queryOption);
    // verify result
    JSONArray ja = JSONArray.parseArray(result.getContent());
    Assert.assertEquals(1, ja.size());
    JSONObject jo = ja.getJSONObject(0);
    Assert.assertEquals("pv1", jo.getString("prop1"));
    Assert.assertEquals("pv2", jo.getString("prop2"));
    Assert.assertEquals("npv1", jo.getJSONObject("nav1").getString("navProp1"));
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.GET, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertNull(argumentCaptor.getValue().getMethodTunnel());
  }

  @Test
  public void testProcessBatchRequestSuccess() {
    List<BatchRequestPart> parts = new ArrayList<BatchRequestPart>();
    // mock retrieve request
    BatchRequestPart retrieveReq1 = new RetrieveRequest("TestEntityType1", mock(FeedQueryOption.class));
    BatchRequestPart retrieveReq2 = new RetrieveRequest("TestEntityType2", "TestKeyValue", mock(EntryQueryOption.class));
    LinkedHashMap<String, String> keyMap = new LinkedHashMap<String, String>();
    keyMap.put("TestKey1", "TestValue1");
    keyMap.put("TestKey2", "TestValue2");
    BatchRequestPart retrieveReq3 = new RetrieveRequest("TestEntityType3", keyMap, mock(EntryQueryOption.class));
    // mock change set
    ChangeSet cs = new ChangeSet();
    ChangeRequestBuilder crBuilder = new ChangeRequestBuilder();
    cs.addChangeRequest(crBuilder.buildInsertRequest(mock(ODataEntity.class)));
    cs.addChangeRequest(crBuilder.buildUpsertRequest(mock(ODataEntity.class)));
    cs.addChangeRequest(crBuilder.buildDeleteRequest("TestEntityType4", "TestKey4"));
    cs.addChangeRequest(crBuilder.buildDeleteRequest("TestEntityType4", keyMap));
    cs.addChangeRequest(crBuilder.buildMergeRequest("TestEntityType5", "TestKey5", mock(ODataEntity.class)));
    cs.addChangeRequest(crBuilder.buildMergeRequest("TestEntityType5", keyMap, mock(ODataEntity.class)));
    cs.addChangeRequest(crBuilder.buildReplaceRequest("TestEntityType6", "TestKey6", mock(ODataEntity.class)));
    cs.addChangeRequest(crBuilder.buildReplaceRequest("TestEntityType6", keyMap, mock(ODataEntity.class)));
    parts.add(retrieveReq1);
    parts.add(retrieveReq2);
    parts.add(retrieveReq3);
    parts.add(cs);
    BatchRequest request = new BatchRequest(parts);
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    httpResponse.setContent("TestContent");
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq("https://testhost/odata/v2/$batch"), argumentCaptor.capture())).thenReturn(httpResponse);
    // run test method
    WrappedResponse result = testObject.processBatchRequest(request);
    // verify result
    Assert.assertEquals("TestContent", result.getContent());
    Assert.assertEquals("TestMessage", result.getMessage());
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.POST, argumentCaptor.getValue().getHttpMethod());
    Assert.assertTrue(argumentCaptor.getValue().getContentType().startsWith("multipart/mixed; boundary="));
    Assert.assertNull(argumentCaptor.getValue().getAcceptType());
    Assert.assertNull(argumentCaptor.getValue().getMethodTunnel());
  }

  @Test
  public void testGetEntityMetadata() {
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    httpResponse.setContent("TestContent");
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq("https://testhost/odata/v2/TestEntityType/$metadata"), argumentCaptor.capture()))
        .thenReturn(httpResponse);
    // run test method
    WrappedResponse result = testObject.getEntityMetadata("TestEntityType");
    // verify result
    Assert.assertEquals("TestContent", result.getContent());
    Assert.assertEquals("TestMessage", result.getMessage());
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.GET, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_XML_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertNull(argumentCaptor.getValue().getContentType());
    Assert.assertNull(argumentCaptor.getValue().getMethodTunnel());
  }

  @Test
  public void testDeleteEntry() {
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    httpResponse.setContent("TestContent");
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq("https://testhost/odata/v2/TestEntityType('TestKey')"), argumentCaptor.capture()))
        .thenReturn(httpResponse);
    // run test method
    WrappedResponse result = testObject.deleteEntry("TestEntityType", "TestKey");
    // verify result
    Assert.assertEquals("TestContent", result.getContent());
    Assert.assertEquals("TestMessage", result.getMessage());
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.POST, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getContentType());
    Assert.assertEquals(METHOD_TUNNEL_DELETE, argumentCaptor.getValue().getMethodTunnel());
  }

  @Test
  public void testDeleteEntryWithMultiKeys() {
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    httpResponse.setContent("TestContent");
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(
        httpClient.execute(eq("https://testhost/odata/v2/TestEntityType(testKey1='testValue1',testKey2='testValue2')"),
            argumentCaptor.capture())).thenReturn(httpResponse);
    // run test method
    LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();
    keys.put("testKey1", "testValue1");
    keys.put("testKey2", "testValue2");
    WrappedResponse result = testObject.deleteEntry("TestEntityType", keys);
    // verify result
    Assert.assertEquals("TestContent", result.getContent());
    Assert.assertEquals("TestMessage", result.getMessage());
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.POST, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getContentType());
    Assert.assertEquals(METHOD_TUNNEL_DELETE, argumentCaptor.getValue().getMethodTunnel());
  }

  @Test
  public void testMergeEntry() {
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    String entityJSON = "{\"d\": {\"prop1\": \"pv1\",\"prop2\": \"pv2\",\"nav1\": {\"navProp1\": \"npv1\"},\"nav2\": {\"navProp2\": \"npv2\"}}}";
    httpResponse.setContent(entityJSON);
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq("https://testhost/odata/v2/TestEntityType('TestKey')"), argumentCaptor.capture()))
        .thenReturn(httpResponse);
    // run test method
    WrappedResponse result = testObject.mergeEntry("TestEntityType", "TestKey", mock(ODataEntity.class));
    // verify result
    Assert.assertEquals("TestMessage", result.getMessage());
    JSONObject jo = JSONObject.parseObject(result.getContent());
    Assert.assertNotNull(jo);
    Assert.assertEquals("pv1", jo.getString("prop1"));
    Assert.assertEquals("pv2", jo.getString("prop2"));
    Assert.assertEquals("npv1", jo.getJSONObject("nav1").getString("navProp1"));
    Assert.assertEquals("npv2", jo.getJSONObject("nav2").getString("navProp2"));
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.POST, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getContentType());
    Assert.assertEquals(METHOD_TUNNEL_MERGE, argumentCaptor.getValue().getMethodTunnel());
  }

  @Test
  public void testReplaceEntry() {
    // mock dependencies
    WrappedResponse httpResponse = new WrappedResponse();
    httpResponse.setSuccess(true);
    httpResponse.setMessage("TestMessage");
    httpResponse.setContent("TestContent");
    ArgumentCaptor<HttpParams> argumentCaptor = ArgumentCaptor.forClass(HttpParams.class);
    when(httpClient.execute(eq("https://testhost/odata/v2/TestEntityType('TestKey')"), argumentCaptor.capture()))
        .thenReturn(httpResponse);
    // run test method
    WrappedResponse result = testObject.replaceEntry("TestEntityType", "TestKey", mock(ODataEntity.class));
    // verify result
    Assert.assertEquals("TestMessage", result.getMessage());
    Assert.assertEquals("TestContent", result.getContent());
    // verify HttpParams
    Assert.assertEquals("TestSFAuthString", argumentCaptor.getValue().getAuthorization());
    Assert.assertEquals(HttpMethod.POST, argumentCaptor.getValue().getHttpMethod());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getAcceptType());
    Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, argumentCaptor.getValue().getContentType());
    Assert.assertEquals(METHOD_TUNNEL_PUT, argumentCaptor.getValue().getMethodTunnel());
  }
}
