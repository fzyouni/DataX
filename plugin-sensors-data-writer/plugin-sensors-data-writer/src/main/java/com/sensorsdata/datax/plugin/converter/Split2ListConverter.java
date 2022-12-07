package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.common.SensorsColumn;
import com.sensorsdata.datax.plugin.convert.Converter;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class Split2ListConverter implements Converter {

  private static final String SEPARATOR = ",|";

  @Override
  public SensorsColumn transform(SensorsColumn column, Map<String, Object> param) {
    Object value = column.getColumnData();
    if (Objects.isNull(value)) {
      return new SensorsColumn();
    }
    String separator = param == null ? SEPARATOR : param.getOrDefault("separator", SEPARATOR).toString();
    return new SensorsColumn(SensorsColumn.SensorsType.LIST,
        Arrays.asList(StringUtils.split(value.toString(), separator)));
  }
}
