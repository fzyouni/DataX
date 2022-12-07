package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.analytics.javasdk.bean.EventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMEventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMUserRecord;
import com.sensorsdata.analytics.javasdk.bean.ItemRecord;
import com.sensorsdata.analytics.javasdk.bean.UserRecord;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import java.util.Map;

/**
 *
 */
public abstract class AbstractSensorData implements SensorData {

  @Override
  public EventRecord convertEventRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException {
    return null;
  }

  @Override
  public UserRecord convertUserRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException {
    return null;
  }

  @Override
  public ItemRecord convertItemRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException {
    return null;
  }

  @Override
  public IDMEventRecord convertEventRecord3(Map<String, Object> fieldsMap) throws InvalidArgumentException {
    return null;
  }

  @Override
  public IDMUserRecord convertUserRecord3(Map<String, Object> fieldsMap) throws InvalidArgumentException {
    return null;
  }

  @Override
  public boolean isIdm3() {
    return false;
  }
}
