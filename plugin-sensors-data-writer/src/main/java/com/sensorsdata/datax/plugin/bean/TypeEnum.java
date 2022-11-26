package com.sensorsdata.datax.plugin.bean;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum TypeEnum {

  EVENT,
  USER,
  ITEM,
  UNKNOWN;

  public static TypeEnum getEventTypeByName(String name) {
    TypeEnum[] values = TypeEnum.values();
    return Arrays.stream(values).filter(it -> StringUtils.equalsIgnoreCase(it.name(), name)).findFirst()
        .orElse(TypeEnum.UNKNOWN);
  }


}
