package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.convert.Converter;

import java.util.Map;
import java.util.Objects;

/**
 * null 值转换器
 */
public class IfNullConverter implements Converter {

  private static final String NULL = "NULL";

  @Override
  public Object transform(Object value, Map<String, Object> param) {
    if (Objects.nonNull(value)) {
      return value;
    }
    return param == null ? NULL : param.getOrDefault("default", NULL);
  }
}
