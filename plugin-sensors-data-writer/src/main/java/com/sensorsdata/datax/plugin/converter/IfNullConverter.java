package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.common.SensorsColumn;
import com.sensorsdata.datax.plugin.convert.Converter;

import cn.hutool.core.util.NumberUtil;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * null 值转换器
 */
public class IfNullConverter implements Converter {

  private static final String DEFAULT = "default";


  @Override
  public SensorsColumn transform(SensorsColumn column, Map<String, Object> param) {
    Object value = column.getColumnData();
    if (Objects.nonNull(value)) {
      return column;
    }
    switch (column.getType()) {
      case STRING:
        String str = param == null ? "" : param.getOrDefault(DEFAULT, "").toString();
        return new SensorsColumn(SensorsColumn.SensorsType.STRING, str);
      case DATE:
        Date date = param == null ?
            new Date() :
            new Date(Long.parseLong(param.getOrDefault(DEFAULT, new Date().getTime()).toString()));
        return new SensorsColumn(SensorsColumn.SensorsType.DATE, date);
      case NUMBER:
        Number number = param == null ? 0 : NumberUtil.parseNumber(param.getOrDefault(DEFAULT, 0).toString());
        return new SensorsColumn(SensorsColumn.SensorsType.NUMBER, number);
      case BOOLEAN:
        Boolean bool = param != null && Boolean.getBoolean(param.getOrDefault(DEFAULT, false).toString());
        return new SensorsColumn(SensorsColumn.SensorsType.BOOLEAN, bool);
      case LIST:
        return new SensorsColumn(SensorsColumn.SensorsType.LIST, Collections.EMPTY_LIST);
      default:
        return new SensorsColumn();
    }
  }

}
