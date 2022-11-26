package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.datax.plugin.exception.SaErrorEnum;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
public class SensorColumn implements Serializable {

  private Integer index;

  private String name;

  //是否丢弃整行数据
  private Boolean ifNullGiveUp = false;

  private List<DataConverter> dataConverters;

  public CheckResult check() {
    if (Objects.isNull(index)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_COLUMN_INDEX).build();
    }
    if (Objects.isNull(name)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_COLUMN_NAME).build();
    }

    return CheckResult.builder().success(true).build();
  }
}
