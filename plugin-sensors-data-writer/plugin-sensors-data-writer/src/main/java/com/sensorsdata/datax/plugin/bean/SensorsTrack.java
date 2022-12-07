package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.analytics.javasdk.bean.EventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMEventRecord;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.datax.plugin.exception.SaErrorEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 神策 track 事件节点信息
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorsTrack extends AbstractSensorData {

  /**
   * 是否为 IDM3.0
   */
  private Boolean idm3 = false;
  /**
   * IDM3.0 用户纬度信息
   */
  private List<SensorsIdentity> identities;
  /**
   * 事件名,若填该值，则生成事件名都一样
   */
  private String eventName;
  /**
   * 事件名,若填该值，则根据 column 来生成事件名
   */
  private String eventNameColumn;
  /**
   * 神策 distinctId 对应的列
   */
  private String distinctIdColumn;
  /**
   * 神策 distinctId 对应的固定值
   */
  private String distinctIdName;
  /**
   * distinctId 是否为登录 ID
   */
  private Boolean isLoginId = false;

  public CheckResult check(List<SensorColumn> columns) {
    if (idm3) {
      if (identities == null || identities.size() == 0) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_IDENTITIES).build();
      }
      for (SensorsIdentity identity : identities) {
        CheckResult check = identity.check(columns);
        if (!check.getSuccess()) {
          return check;
        }
      }
    } else {
      if (StringUtils.isBlank(distinctIdColumn) && StringUtils.isBlank(distinctIdName)) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_IDENTITY_DISTINCT_ID).build();
      }
    }
    if (StringUtils.isBlank(eventName) && StringUtils.isBlank(eventNameColumn)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_TRACK_EVENT_NAME).build();
    }
    if (StringUtils.isNotBlank(eventNameColumn)) {
      boolean b = columns.stream().anyMatch(it -> StringUtils.equals(it.getName(), eventNameColumn));
      if (!b) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_EVENT_NAME_COLUMN).build();
      }
    }
    if (StringUtils.isNotBlank(distinctIdColumn)) {
      boolean b = columns.stream().anyMatch(it -> StringUtils.equals(it.getName(), distinctIdColumn));
      if (!b) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_DISTINCT_ID_COLUMN).build();
      }
    }
    return CheckResult.builder().success(true).build();
  }

  @Override
  public EventRecord convertEventRecord(Map<String, Object> fieldMaps) throws InvalidArgumentException {
    String eventNameValue =
        StringUtils.isNotBlank(eventName) ? eventName : fieldMaps.remove(eventNameColumn).toString();
    String distinctId =
        StringUtils.isNotBlank(distinctIdName) ? distinctIdName : fieldMaps.remove(distinctIdColumn).toString();
    return EventRecord.builder()
        .setEventName(eventNameValue)
        .setDistinctId(distinctId)
        .isLoginId(isLoginId)
        .addProperties(fieldMaps)
        .build();
  }

  @Override
  public IDMEventRecord convertEventRecord3(Map<String, Object> fieldMaps) throws InvalidArgumentException {
    String eventNameValue =
        StringUtils.isNotBlank(eventName) ? eventName : fieldMaps.remove(eventNameColumn).toString();
    String distinctId =
        StringUtils.isNotBlank(distinctIdName) ? distinctIdName : fieldMaps.remove(distinctIdColumn).toString();
    Map<String, String> identitiesMap = identities.stream().map(it -> it.convertIdentity(fieldMaps))
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    if (StringUtils.isBlank(distinctId)) {
      return IDMEventRecord.starter()
          .setEventName(eventNameValue)
          .identityMap(identitiesMap)
          .addProperties(fieldMaps)
          .build();
    }
    return IDMEventRecord.starter()
        .setEventName(eventNameValue)
        .identityMap(identitiesMap)
        .setDistinctId(distinctId)
        .addProperties(fieldMaps)
        .build();
  }

  @Override
  public boolean isIdm3() {
    return this.idm3;
  }

}
