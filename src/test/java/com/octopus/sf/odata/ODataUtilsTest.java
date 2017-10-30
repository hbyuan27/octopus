package com.octopus.sf.odata;

import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.octopus.AbstractUnitTest;
import com.octopus.sf.odata.EntryQueryOption;
import com.octopus.sf.odata.FeedQueryOption;
import com.octopus.sf.odata.FilterOption;
import com.octopus.sf.odata.ODataUtils;
import com.octopus.sf.odata.QueryFilterEnum;
import com.octopus.sf.odata.EntryQueryOption.AcceptFormatEnum;

public class ODataUtilsTest extends AbstractUnitTest {

  private String entityTypeName;

  private String valueOfBusinessKey;

  private LinkedHashMap<String, String> businessKeyMap;

  @Before
  public void setUp() {
    entityTypeName = "testEntityTypeName";
    valueOfBusinessKey = "testValueOfBusinessKey";
    businessKeyMap = new LinkedHashMap<String, String>();
    businessKeyMap.put("keyA", "valueA");
    businessKeyMap.put("keyB", "valueB");
  }

  @Test
  public void testBuildResourceUriWithSingleKey() {
    String uri = ODataUtils.buildResourceUri(entityTypeName, valueOfBusinessKey);
    Assert.assertEquals("testEntityTypeName('testValueOfBusinessKey')", uri);
  }

  @Test
  public void testBuildResourceUriWithMultipleKey() {
    String uri = ODataUtils.buildResourceUri(entityTypeName, businessKeyMap);
    Assert.assertEquals("testEntityTypeName(keyA='valueA',keyB='valueB')", uri);
  }

  @Test
  public void testNullAndEmptyOptions() {
    String uri = ODataUtils.buildQueryOptionUri(null);
    Assert.assertEquals("", uri);

    EntryQueryOption entryQueryOption = new EntryQueryOption();
    uri = ODataUtils.buildQueryOptionUri(entryQueryOption);
    Assert.assertEquals("", uri);

    FeedQueryOption feedQueryOption = new FeedQueryOption();
    uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("", uri);
  }

  @Test
  public void testBuildQueryOptionUriForEntry1() {
    EntryQueryOption entryQueryOption = new EntryQueryOption();

    entryQueryOption.addSelectOption("s1");
    entryQueryOption.addSelectOption("s2");
    entryQueryOption.addSelectOption("s3");
    String uri = ODataUtils.buildQueryOptionUri(entryQueryOption);
    Assert.assertEquals("?$select=s1,s2,s3", uri);

    entryQueryOption.addExpandOption("exp1");
    entryQueryOption.addExpandOption("exp2");
    entryQueryOption.addExpandOption("exp3");
    uri = ODataUtils.buildQueryOptionUri(entryQueryOption);
    Assert.assertEquals("?$select=s1,s2,s3&$expand=exp1,exp2,exp3", uri);

    entryQueryOption.setAcceptFormat(AcceptFormatEnum.JSON);
    uri = ODataUtils.buildQueryOptionUri(entryQueryOption);
    Assert.assertEquals("?$select=s1,s2,s3&$expand=exp1,exp2,exp3&$format=json", uri);
  }

  @Test
  public void testBuildQueryOptionUriForEntry2() {
    EntryQueryOption entryQueryOption = new EntryQueryOption();

    entryQueryOption.addExpandOption("exp1");
    entryQueryOption.addExpandOption("exp2");
    entryQueryOption.addExpandOption("exp3");
    String uri = ODataUtils.buildQueryOptionUri(entryQueryOption);
    Assert.assertEquals("?$expand=exp1,exp2,exp3", uri);

    entryQueryOption.setAcceptFormat(AcceptFormatEnum.JSON);
    uri = ODataUtils.buildQueryOptionUri(entryQueryOption);
    Assert.assertEquals("?$expand=exp1,exp2,exp3&$format=json", uri);
  }

  @Test
  public void testBuildQueryOptionUriForFeed() {
    FeedQueryOption feedQueryOption = new FeedQueryOption();
    feedQueryOption.addOrderByOption("ob1");
    feedQueryOption.addOrderByOption("ob2");
    String uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("?$orderby=ob1,ob2", uri);

    feedQueryOption.setOrderByDesc(true);
    uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("?$orderby=ob1,ob2 desc", uri);

    feedQueryOption.setTop(5);
    feedQueryOption.setSkip(3);
    uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("?$top=5&$skip=3&$orderby=ob1,ob2 desc", uri);

    FilterOption filterOption = new FilterOption("filterClause");
    feedQueryOption.setFilterOption(filterOption);
    uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("?$top=5&$skip=3&$orderby=ob1,ob2 desc&$filter=filterClause", uri);

    feedQueryOption.addSelectOption("s1");
    feedQueryOption.addSelectOption("s2");
    uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("?$select=s1,s2&$top=5&$skip=3&$orderby=ob1,ob2 desc&$filter=filterClause", uri);
  }

  @Test
  public void testFilterOption() {
    FeedQueryOption feedQueryOption = new FeedQueryOption();

    FilterOption filterOption = new FilterOption("p1", QueryFilterEnum.GREATER_THAN, "v1");
    feedQueryOption.setFilterOption(filterOption);
    String uri = ODataUtils.buildQueryOptionUri(feedQueryOption);
    Assert.assertEquals("?$filter=p1%20gt%20'v1'", uri);
  }

}