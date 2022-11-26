package com.sensorsdata.datax.plugin.bean;


import com.sensorsdata.datax.plugin.exception.SaErrorEnum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckResult {
  /**
   * 校验结果
   */
  @Builder.Default
  private Boolean success = false;
  /**
   * 错误类型枚举
   */
  private SaErrorEnum errorEnum;
}
