package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.analytics.javasdk.bean.IDMUserRecord;
import com.sensorsdata.analytics.javasdk.bean.UserRecord;
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
 * 神策 profile 数据节点
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorsProfile extends AbstractSensorData {
  /**
   * 是否为 IDM3.0
   */
  private Boolean idm3 = false;
  /**
   * IDM3.0 用户纬度信息
   */
  private List<SensorsIdentity> identities;
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
    } else {
      if (StringUtils.isBlank(distinctIdColumn) && StringUtils.isBlank(distinctIdName)) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_IDENTITY_DISTINCT_ID).build();
      }
    }
    if (StringUtils.isNotBlank(distinctIdColumn)) {
      boolean b = columns.stream().anyMatch(it -> StringUtils.equals(distinctIdColumn, it.getName()));
      if (!b) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_DISTINCT_ID_COLUMN).build();
      }
    }
    for (SensorsIdentity identity : identities) {
      CheckResult check = identity.check(columns);
      if (!check.getSuccess()) {
        return check;
      }
    }
    return CheckResult.builder().success(true).build();
  }

  @Override
  public UserRecord convertUserRecord(Map<String, Object> fieldMaps) throws InvalidArgumentException {
    String distinctId =
        StringUtils.isBlank(distinctIdColumn) ? distinctIdName : fieldMaps.remove(distinctIdColumn).toString();
    return UserRecord.builder()
        .setDistinctId(distinctId)
        .isLoginId(isLoginId)
        .addProperties(fieldMaps)
        .build();
  }

  @Override
  public IDMUserRecord convertUserRecord3(Map<String, Object> fieldMaps) throws InvalidArgumentException {
    Map<String, String> identitiesMap = identities.stream().map(it -> it.convertIdentity(fieldMaps)).collect(
        Collectors.toMap(Pair::getKey, Pair::getValue, (v1, v2) -> v2));
    String distinctId =
        StringUtils.isNotBlank(distinctIdColumn) ? fieldMaps.remove(distinctIdColumn).toString() : distinctIdName;
    if (StringUtils.isBlank(distinctId)) {
      return IDMUserRecord.starter()
          .identityMap(identitiesMap)
          .addProperties(fieldMaps)
          .build();
    }
    return IDMUserRecord.starter()
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
