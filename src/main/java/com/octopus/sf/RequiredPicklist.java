package com.octopus.sf;

import java.util.ArrayList;
import java.util.List;

public class RequiredPicklist {

  public static final String ID_YESNO = "yesno";

  public static final String ID_YESNONA = "yesnoNa";

  private static final List<String> PICKLISTS = new ArrayList<String>(4);

  static {
    PICKLISTS.add(ID_YESNO);
    PICKLISTS.add(ID_YESNONA);
  }

  public static List<String> getPicklists() {
    return PICKLISTS;
  }

}
