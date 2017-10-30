package com.octopus.sf.odata.batch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.octopus.AbstractUnitTest;
import com.octopus.sf.common.CommonUtils;
import com.octopus.sf.odata.batch.BatchResponse;
import com.octopus.sf.odata.batch.BatchResponseParser;

public class BatchResponseParserTest extends AbstractUnitTest {

  private BatchResponseParser testObject;

  @Before
  public void setUp() {
    testObject = new BatchResponseParser();
  }

  @Test
  public void testParseBatchResponseSuccess() {
    String content = null;
    // read test batch response file
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("odata/batch/response_body_example");
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    try {
      int length = 0;
      while ((length = is.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
      content = result.toString(StandardCharsets.UTF_8.name());
    } catch (IOException e) {
    } finally {
      CommonUtils.closeQuietly(is);
    }
    Assert.assertNotNull(content);
    BatchResponse response = testObject.parseBatchResponse(content);
    Assert.assertEquals(4, response.getParts().size());
    Assert.assertEquals("200 OK", response.getPart(0).getResponseCode());
    Assert.assertEquals("201 Created", response.getPart(1).getResponseCode());
    Assert.assertEquals("application/json; charset=utf-8", response.getPart(1).getContentType());
    Assert.assertEquals("200 OK", response.getPart(2).getResponseCode());
    Assert.assertEquals("application/atom+xml; charset=utf-8", response.getPart(2).getContentType());
    Assert.assertEquals("200 OK", response.getPart(3).getResponseCode());
  }

}
