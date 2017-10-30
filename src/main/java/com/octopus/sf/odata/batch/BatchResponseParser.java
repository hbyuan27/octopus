package com.octopus.sf.odata.batch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.octopus.sf.common.CommonUtils;

@Component
public class BatchResponseParser {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public BatchResponse parseBatchResponse(String content) {
    // validate
    content = content.trim();
    String boundary = content.substring(0, 44); // UUID has a fixed length
    if (!content.startsWith(boundary) || !content.endsWith(boundary + "--")) {
      logger.error("the multipart response is not in valid format.");
      return null;
    }
    BatchResponse response = new BatchResponse();
    // remove the first and last boundary
    content = content.substring(boundary.length(), content.length() - (boundary + "--").length());
    String[] parts = StringUtils.delimitedListToStringArray(content, boundary);
    for (String part : parts) {
      part = part.trim();
      if (part.contains("Content-Type: multipart/mixed; boundary=changeset_")) {
        String changeSetBoundary = part.substring(40, 86); // UUID has a fixed length
        response.addParts(parseChangeSetResponse(part, changeSetBoundary));
      } else if (part.contains("Content-Type: application/http")) {
        response.addPart(parseRetrieveResponse(part));
      } else {
        logger.error("the Content-Type of this response part is invalid");
        continue;
      }
    }
    return response;
  }

  private List<BatchResponsePart> parseChangeSetResponse(String content, String boundary) {
    List<BatchResponsePart> responseParts = new ArrayList<BatchResponsePart>();
    boundary = "--" + boundary;
    // remove the last line - the boundary end mark
    content = content.substring(0, content.length() - boundary.length() - 2);
    String[] parts = StringUtils.delimitedListToStringArray(content, boundary);
    for (int i = 0; i < parts.length; i++) {
      // skip the first one which is not a real change response
      if (i == 0) {
        continue;
      }
      responseParts.add(parseRetrieveResponse(parts[i].trim()));
    }
    return responseParts;
  }

  private BatchResponsePart parseRetrieveResponse(String part) {
    BatchResponsePart responsePart = new BatchResponsePart();
    // parse the text in the part
    BufferedReader br = null;
    try {
      InputStream is = new ByteArrayInputStream(part.getBytes());
      br = new BufferedReader(new InputStreamReader(is));
      String line = null;
      int lineNumber = 0;
      int startLineOfBody = Integer.MAX_VALUE;
      StringBuilder sb = new StringBuilder();
      StringBuilder sbLine = new StringBuilder();
      int charCode;
      while ((charCode = br.read()) != -1) {
        char c = (char) charCode;
        if ((c == '\n') || (c == '\r')) {
          line = sbLine.toString();
          sbLine = new StringBuilder();
          lineNumber++;
          if (lineNumber <= 3) {
            // skip the first 3 lines
            continue;
          }
          if (line.startsWith("HTTP/1.1")) {
            responsePart.setResponseCode(line.substring(9));
          }
          if (line.startsWith("Content-Type:")) {
            responsePart.setContentType(line.substring(14));
          }
          if (line.isEmpty()) {
            // when reach the empty line, means the coming context is the body
            startLineOfBody = lineNumber;
          }
          if (lineNumber > startLineOfBody) {
            sb.append(line);
          }
          continue;
        }
        sbLine.append(c);
      }
      sb.append(sbLine);
      String body = sb.toString().trim();
      String contentType = responsePart.getContentType();
      if (contentType != null && contentType.contains("application/json")) {
        JSONObject jo = JSONObject.parseObject(body);
        // for success response
        if (jo.containsKey("d")) {
          jo = jo.getJSONObject("d");
          if (jo.containsKey("results")) {
            // TODO convert to list of ODataEntity
            responsePart.setResponseBody(jo.getJSONArray("results"));
          } else {
            // TODO convert to ODataEntity
            responsePart.setResponseBody(jo);
          }
        }
        // for failed response
        if (jo.containsKey("error")) {
          jo = jo.getJSONObject("error");
          responsePart.setResponseBody(jo);
        }
      } else if (contentType != null && contentType.contains("application/atom+xml")) {
        // TODO parse xml
        responsePart.setResponseBody(body);
      } else {
        logger.error("The retrieve response of batch request is not a json or xml response.");
      }
    } catch (IOException | RuntimeException e) {
      logger.error("Parse batch response part failed due to: " + e.getMessage());
    } finally {
      CommonUtils.closeQuietly(br);
    }
    return responsePart;
  }
}
