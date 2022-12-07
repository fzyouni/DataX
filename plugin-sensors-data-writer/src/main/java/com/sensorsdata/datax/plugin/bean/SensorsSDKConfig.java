package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.datax.plugin.exception.SaErrorEnum;
import com.sensorsdata.datax.plugin.util.SaSdkFactory;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * sdk 配置文件
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/11/30 14:07
 */
@Data
public class SensorsSDKConfig {
  /**
   * 日志保存地址
   */
  private String logPathAddress;
  /**
   * 数据发送地址
   */
  private String dataServer;
  /**
   * sdk 缓存大小
   */
  private Integer bulkSize = 50;
  /**
   * 网络发送超时设置
   */
  private Integer timeoutSec = 3;

  public CheckResult check() {
    if (StringUtils.isBlank(logPathAddress) && StringUtils.isBlank(dataServer)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_SDK_ADDRESS).build();
    }
    if (StringUtils.isNotBlank(logPathAddress)) {
      File file = new File(logPathAddress);
      if (!file.getParentFile().exists()) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_FILE_PATH).build();
      }
    }
    if (StringUtils.isNotBlank(dataServer)) {
      try {
        new URI(dataServer);
      } catch (URISyntaxException e) {
        return CheckResult.builder().errorEnum(SaErrorEnum.ERROR_SDK_URI).build();
      }
    }
    return CheckResult.builder().success(true).build();
  }

  public void initSDK() {
    if (StringUtils.isNotBlank(logPathAddress)) {
      SaSdkFactory.SELF.initSdkLogAddress(logPathAddress, bulkSize);
    }
    if (StringUtils.isNotBlank(dataServer)) {
      SaSdkFactory.SELF.initSdkNetAddress(dataServer, bulkSize, timeoutSec);
    }
  }
}
