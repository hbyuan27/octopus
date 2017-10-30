package com.octopus.sf.service;

import static com.octopus.sf.SFConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.octopus.sf.RequiredPicklist;
import com.octopus.sf.common.WrappedResponse;
import com.octopus.sf.dao.SFPicklistOptionDAO;
import com.octopus.sf.odata.FeedQueryOption;
import com.octopus.sf.odata.FilterOption;
import com.octopus.sf.odata.QueryFilterEnum;
import com.octopus.sf.odata.SFODataService;
import com.octopus.sf.po.SFPicklistOption;

@Service
public class SFPicklistService {

  @Autowired
  private SFPicklistOptionDAO optionDAO;

  @Autowired
  private SFODataService odataService;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public String getDefaultOption(String picklist) {
    String defaultLabel = null;
    switch (picklist) {
    case RequiredPicklist.ID_YESNO:
      defaultLabel = "Yes";
      break;
    case RequiredPicklist.ID_YESNONA:
      defaultLabel = "NA";
      break;
    default:
      logger.error("Invalid picklist id: " + picklist);
      break;
    }
    SFPicklistOption probe = new SFPicklistOption();
    probe.setPicklistId(picklist);
    probe.setDefaultLabel(defaultLabel);
    SFPicklistOption entity = optionDAO.findOne(Example.of(probe));
    return (entity == null) ? null : entity.getOptionId();
  }

  public void cachePicklists() {
    for (String picklist : RequiredPicklist.getPicklists()) {
      Map<String, String> options = fetchPicklistOptions(picklist);
      if (options == null) {
        logger.error("Fetch PicklistOption failed, picklist id: " + picklist);
        continue;
      }
      try {
        persistPicklist(picklist, options);
      } catch (RuntimeException e) {
        logger.error("Failed to cache picklist options into database, picklist id: " + picklist);
      }
    }
  }

  // return the map of picklist option id and its corresponding en_US label for given picklist
  private Map<String, String> fetchPicklistOptions(String picklist) {
    FeedQueryOption option = new FeedQueryOption();
    option.addSelectOption("picklistOptions/id");
    option.addSelectOption("picklistOptions/picklistLabels/locale");
    option.addSelectOption("picklistOptions/picklistLabels/label");
    option.addExpandOption("picklistOptions");
    option.addExpandOption("picklistOptions/picklistLabels");
    FilterOption filterOption = new FilterOption("picklistId", QueryFilterEnum.EQUAL, picklist);
    option.setFilterOption(filterOption);
    // get option id list via OData API calls
    WrappedResponse response = odataService.readFeed(ENTITY_TYPE_PICKLIST, option);
    if (!response.isSuccess()) {
      return null;
    }
    JSONArray ja = JSONObject.parseArray(response.getContent());
    if (ja == null || ja.size() != 1) {
      return null;
    }
    Map<String, String> options = new HashMap<String, String>();
    try {
      JSONArray optionArray = ja.getJSONObject(0).getJSONObject("picklistOptions").getJSONArray("results");
      for (Object optionJSON : optionArray) {
        String optionId = ((JSONObject) optionJSON).getString("id");
        JSONArray labels = ((JSONObject) optionJSON).getJSONObject("picklistLabels").getJSONArray("results");
        for (Object label : labels) {
          if (((JSONObject) label).getString("locale").equals("en_US")) {
            options.put(optionId, ((JSONObject) label).getString("label"));
            break;
          }
        }
      }
    } catch (Exception e) {
      return null;
    }
    return options;
  }

  @Transactional
  private void persistPicklist(String picklist, Map<String, String> options) {
    for (Entry<String, String> entry : options.entrySet()) {
      String optionId = entry.getKey();
      String defaultLabel = entry.getValue();
      // save or update data into database to reduce OData API calls
      SFPicklistOption probe = new SFPicklistOption();
      probe.setPicklistId(picklist);
      probe.setOptionId(optionId);
      SFPicklistOption entity = optionDAO.findOne(Example.of(probe));
      if (entity == null) {
        entity = new SFPicklistOption();
      }
      entity.setPicklistId(picklist);
      entity.setOptionId(optionId);
      // use en_US label as the default label
      entity.setDefaultLabel(defaultLabel);
      optionDAO.save(entity);
    }
  }
}
