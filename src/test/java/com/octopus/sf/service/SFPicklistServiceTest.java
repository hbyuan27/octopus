package com.octopus.sf.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.octopus.AbstractDataJpaTest;
import com.octopus.sf.RequiredPicklist;
import com.octopus.sf.common.WrappedResponse;
import com.octopus.sf.dao.SFPicklistOptionDAO;
import com.octopus.sf.odata.FeedQueryOption;
import com.octopus.sf.odata.SFODataService;
import com.octopus.sf.po.SFPicklistOption;
import com.octopus.sf.service.SFPicklistService;

public class SFPicklistServiceTest extends AbstractDataJpaTest {

  @Autowired
  private SFPicklistService testObject;

  @Autowired
  private SFPicklistOptionDAO optionDAO;

  @Autowired
  private SFODataService odataService;

  @Autowired
  public void setUp() {
    odataService = Mockito.mock(SFODataService.class);
    ReflectionTestUtils.setField(testObject, "odataService", odataService);
    // mock picklist data
    String expectedJSON = "[{\"picklistOptions\": {\"results\": [{\"picklistLabels\": {\"results\": [{\"label\": \"Yes\",\"locale\": \"en_US\"}]},\"id\": \"1234\"},{\"picklistLabels\": {\"results\": [{\"label\": \"No\",\"locale\": \"en_US\"}]},\"id\": \"2345\"},{\"picklistLabels\": {\"results\": [{\"label\": \"NA\",\"locale\": \"en_US\"}]},\"id\": \"3456\"}]}}]";
    WrappedResponse response = new WrappedResponse();
    response.setSuccess(true);
    response.setMessage("TestMessage");
    response.setContent(expectedJSON);
    Mockito.when(odataService.readFeed(Mockito.anyString(), Mockito.any(FeedQueryOption.class))).thenReturn(response);
  }

  @Test
  public void testFetchPicklistOptions() {
    List<SFPicklistOption> options = optionDAO.findAll();
    Assert.assertEquals(0, options.size());
    // run method
    testObject.cachePicklists();
    // check database again
    options = optionDAO.findAll();
    Assert.assertEquals(6, options.size());
  }

  @Test
  public void testGetDefaultOption() {
    testObject.cachePicklists();
    Assert.assertEquals("1234", testObject.getDefaultOption(RequiredPicklist.ID_YESNO));
    Assert.assertEquals("3456", testObject.getDefaultOption(RequiredPicklist.ID_YESNONA));
  }

}
