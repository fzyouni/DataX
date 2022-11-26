package com.sensorsdata.datax.plugin.exception;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.spi.ErrorCode;


public class SaDataXException extends DataXException {

  public SaDataXException(ErrorCode errorCode) {
    super(errorCode, errorCode.getDescription());
  }
}
