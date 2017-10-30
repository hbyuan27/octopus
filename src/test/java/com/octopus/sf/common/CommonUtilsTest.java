package com.octopus.sf.common;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.octopus.AbstractUnitTest;
import com.octopus.sf.common.CommonUtils;

public class CommonUtilsTest extends AbstractUnitTest {

  @Test
  public void testFormatURL() {
    String testURL1 = "http://testhost/resource/";
    String testURL2 = "http://testhost/resource";
    String result1 = CommonUtils.formatURL(testURL1);
    String result2 = CommonUtils.formatURL(testURL2);
    Assert.assertEquals(testURL1, result1);
    Assert.assertEquals(testURL1, result2);
  }

  @Test
  public void testCloseQuietly() {
    InputStream is = Mockito.spy(InputStream.class);
    CommonUtils.closeQuietly(is);
    try {
      Mockito.verify(is).close();
    } catch (IOException e) {
      Assert.assertFalse(true);
    }
  }
}
