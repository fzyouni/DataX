package com.sensorsdata.datax.plugin.bean;


import com.sensorsdata.analytics.javasdk.bean.EventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMEventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMUserRecord;
import com.sensorsdata.analytics.javasdk.bean.ItemRecord;
import com.sensorsdata.analytics.javasdk.bean.UserRecord;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import java.io.Serializable;
import java.util.Map;

public interface SensorData extends Serializable {

  EventRecord convertEventRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException;

  UserRecord convertUserRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException;

  ItemRecord convertItemRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException;

  IDMEventRecord convertEventRecord3(Map<String, Object> fieldsMap) throws InvalidArgumentException;

  IDMUserRecord convertUserRecord3(Map<String, Object> fieldsMap) throws InvalidArgumentException;

  boolean isIdm3();
}
