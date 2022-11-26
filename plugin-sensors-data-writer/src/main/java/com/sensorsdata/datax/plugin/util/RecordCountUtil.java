package com.sensorsdata.datax.plugin.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * column 统计器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordCountUtil {

  /**
   * 字段为 null，抛弃整条record总数
   */
  private static final Map<String, AtomicLong> NULL_GIVE_UP_COUNT_MAP = new ConcurrentHashMap<>();
  /**
   * 字段为 null,抛弃当前 column
   */
  private static final Map<String, AtomicLong> NULL_COUNT_MAP = new ConcurrentHashMap<>();
  /**
   * 发送记录条数
   */
  private static final Map<String, AtomicLong> RECORD_MAP = new ConcurrentHashMap<>();


  public static void initRecordCount(String type) {
    RECORD_MAP.put(type, new AtomicLong());
  }

  public static void initExcludeCount(String name) {
    NULL_COUNT_MAP.put(name, new AtomicLong());
  }

  public static void initNullGiveUpCount(String name) {
    NULL_GIVE_UP_COUNT_MAP.put(name, new AtomicLong());
  }

  public static void incRecordCount(String type) {
    if (RECORD_MAP.containsKey(type)) {
      RECORD_MAP.get(type).getAndIncrement();
    } else {
      RECORD_MAP.put(type, new AtomicLong(1));
    }
  }

  public static void incExcludeCount(String name) {
    if (NULL_COUNT_MAP.containsKey(name)) {
      NULL_COUNT_MAP.get(name).getAndIncrement();
    } else {
      NULL_COUNT_MAP.put(name, new AtomicLong(1));
    }
  }

  public static void incNullGiveUpCount(String name) {
    if (NULL_GIVE_UP_COUNT_MAP.containsKey(name)) {
      NULL_GIVE_UP_COUNT_MAP.get(name).getAndIncrement();
    } else {
      NULL_GIVE_UP_COUNT_MAP.put(name, new AtomicLong(1));
    }
  }

  public static Map<String, AtomicLong> getRecordMap() {
    return RECORD_MAP;
  }

  public static Map<String, AtomicLong> getNullGiveUpCountMap() {
    return NULL_GIVE_UP_COUNT_MAP;
  }

  public static Map<String, AtomicLong> getNullCountMap() {
    return NULL_COUNT_MAP;
  }
}
