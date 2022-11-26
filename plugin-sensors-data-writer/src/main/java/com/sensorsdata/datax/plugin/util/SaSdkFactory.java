package com.sensorsdata.datax.plugin.util;

import com.sensorsdata.analytics.javasdk.ISensorsAnalytics;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.bean.EventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMEventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMUserRecord;
import com.sensorsdata.analytics.javasdk.bean.ItemRecord;
import com.sensorsdata.analytics.javasdk.bean.UserRecord;
import com.sensorsdata.analytics.javasdk.consumer.BatchConsumer;
import com.sensorsdata.analytics.javasdk.consumer.ConcurrentLoggingConsumer;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

/**
 *
 */
public enum SaSdkFactory {

  SELF;

  private ISensorsAnalytics netInstance;

  private ISensorsAnalytics logInstance;

  public void initSdkNetAddress(String netAddress) {
    netInstance = new SensorsAnalytics(new BatchConsumer(netAddress));
  }

  public void initSdkLogAddress(String logAddress) {
    logInstance = new SensorsAnalytics(new ConcurrentLoggingConsumer(logAddress));
  }

  public ISensorsAnalytics getNetInstance() {
    return netInstance;
  }

  public ISensorsAnalytics getLogInstance() {
    return logInstance;
  }


  public void send(EventRecord eventRecord) throws InvalidArgumentException {
    if (netInstance != null) {
      netInstance.track(eventRecord);
    }
    if (logInstance != null) {
      logInstance.track(eventRecord);
    }
  }

  public void send(IDMEventRecord idmEventRecord) throws InvalidArgumentException {
    if (netInstance != null) {
      netInstance.trackById(idmEventRecord);
    }
    if (logInstance != null) {
      logInstance.trackById(idmEventRecord);
    }
  }

  public void send(UserRecord userRecord) throws InvalidArgumentException {
    if (netInstance != null) {
      netInstance.profileSet(userRecord);
    }
    if (logInstance != null) {
      logInstance.profileSet(userRecord);
    }
  }

  public void send(ItemRecord itemRecord) throws InvalidArgumentException {
    if (netInstance != null) {
      netInstance.itemSet(itemRecord);
    }
    if (logInstance != null) {
      logInstance.itemSet(itemRecord);
    }
  }

  public void send(IDMUserRecord userRecord) throws InvalidArgumentException {
    if (netInstance != null) {
      netInstance.profileSetById(userRecord);
    }
    if (logInstance != null) {
      logInstance.profileSetById(userRecord);
    }
  }


  public void close() {
    if (netInstance != null) {
      netInstance.shutdown();
    }
    if (logInstance != null) {
      logInstance.shutdown();
    }
  }

}
