package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.datax.plugin.exception.SaErrorEnum;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用户信息
 */
@Data
public class SensorsIdentity implements Serializable {

  /**
   * 神策用户 ID 值对应的列名
   */
  private String sensorIdValueColumn;
  /**
   * 神策用户 ID 值固定名称
   */
  private String sensorIdValueName;
  /**
   * 神策用户ID key 对应的列名
   */
  private String sensorIdKeyColumn;
  /**
   * 神策用户ID key 固定名称
   */
  private String sensorsIdKeyName;


  public CheckResult check(List<SensorColumn> columns) {
    if (StringUtils.isBlank(sensorsIdKeyName) && StringUtils.isBlank(sensorIdKeyColumn)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_IDENTITY_KEY).build();
    }
    if (StringUtils.isBlank(sensorIdValueColumn) && StringUtils.isBlank(sensorIdValueName)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_IDENTITY_VALUE).build();
    }
    if (StringUtils.isNotBlank(sensorIdKeyColumn)) {
      boolean b = columns.stream().anyMatch(it -> StringUtils.equals(it.getName(), sensorIdKeyColumn));
      if (!b) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_SENSOR_ID_COLUMN).build();
      }
    }
    if (StringUtils.isNotBlank(sensorIdValueColumn)) {
      boolean b = columns.stream().anyMatch(it -> StringUtils.equals(it.getName(), sensorIdValueColumn));
      if (!b) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_SENSOR_VALUE_COLUMN).build();
      }
    }
    return CheckResult.builder().success(true).build();
  }

  public Pair<String, String> convertIdentity(Map<String, Object> fieldMaps) {
    String key =
        StringUtils.isBlank(sensorIdKeyColumn) ? sensorsIdKeyName : fieldMaps.remove(sensorIdKeyColumn).toString();
    String value =
        StringUtils.isBlank(sensorIdValueColumn) ? sensorIdValueName : fieldMaps.remove(sensorIdValueColumn).toString();
    return Pair.of(key, value);
  }

}
