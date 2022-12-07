package com.sensorsdata.datax.plugin.exception;


public class SaDataXException extends DataXException {

  public SaDataXException(ErrorCode errorCode) {
    super(errorCode, errorCode.getDescription());
  }
}
