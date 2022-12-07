package com.sensorsdata.datax.plugin.exception;


public enum SaErrorEnum implements ErrorCode {

  NONE_KEY_EVENT_TYPE("NO_KEY_EVENT_TYPE", "writer 配置文件中不存在 eventType key."),
  NONE_KEY_COLUMNS("NONE_KEY_COLUMNS", "writer 配置文件中不存在 columns key."),
  NONE_KEY_SDK_ADDRESS("NONE_KEY_SDK_ADDRESS", "writer 配置文件中 sdkNetAddress 和 sdkLogPathAddress 必须要配置其中一个."),
  UNKNOWN_EVENT_TYPE("UNKNOWN_EVENT_TYPE", "writer 配置文件中 type 类型不支持."),
  NONE_KEY_TRACK("NONE_KEY_TRACK", "writer 配置文件中 type=event 但没有配置 track 节点信息."),
  NONE_KEY_PROFILE_SET("NONE_KEY_PROFILE_SET", "writer 配置文件中 type=user 但是没有配置 profileSet 节点信息."),
  NONE_KEY_ITEM_SET("NONE_KEY_ITEM_SET", "writer 配置文件中 type=item 但是没有配置 itemSet 节点信息."),
  NONE_KEY_IDENTITY_KEY("NONE_KEY_IDENTITY_KEY",
      "writer 配置文件中 identity 节点中 idm3=true 但是没有设置 sensorIdKey 的节点信息，sensorsIdKeyName 和 sensorIdKeyColumn 必须配置其中一个."),
  NONE_KEY_IDENTITY_VALUE("NONE_KEY_IDENTITY_VALUE",
      "writer 配置文件中 identity 节点中 idm3=true 但是没有设置 sensorIdValue 的节点信息，sensorIdValueColumn 和 sensorIdValueName 必须配置其中一个."),
  NONE_KEY_IDENTITY_DISTINCT_ID("NONE_KEY_IDENTITY_DISTINCT_ID",
      "writer 配置文件中 identity 节点中 idm3=false 但是没有设置 distinctId 的节点信息，distinctIdName 和 distinctIdColumn 必须配置其中一个."),
  NONE_KEY_ITEM_ID("NONE_KEY_ITEM_ID", "writer 配置文件中 eventType=item 但是没有配置 itemIdColumn 节点."),
  NONE_KEY_ITEM_TYPE("NONE_KEY_ITEM_TYPE",
      "writer 配置文件中 eventType=item 但是没有配置 itemIdType 节点.itemTypeColumn 和 itemType 必须配置其中一个."),
  NONE_KEY_IDENTITIES("NONE_KEY_IDENTITIES", "writer 配置文件中,track/profileSet identities 节点信息为空."),
  DIFF_IDENTITIES_IDM("DIFF_IDENTITIES_IDM",
      "writer 配置文件中,track/profileSet identities 节点 idm 节点不统一，必须统一为 true 或者 false."),
  NONE_KEY_TRACK_EVENT_NAME("NONE_KEY_TRACK_EVENT_NAME", "writer 配置文件中 eventType=track 但是没有配置 eventName 节点信息."),
  NONE_KEY_COLUMN_INDEX("NONE_KEY_COLUMN_INDEX", "columns 中存在 index 为空现象，index 不允许为空."),
  NONE_KEY_COLUMN_NAME("NONE_KEY_COLUMN_NAME", "columns 中存在 name 为空现象，name 不允许为空."),
  NOT_EXIST_SENSOR_ID_COLUMN("NOT_EXIST_SENSOR_ID_COLUMN",
      "神策事件节点中 identities 中 sensorIdKeyColumn 对应的值不存在于 columns 里面."),
  NOT_EXIST_SENSOR_VALUE_COLUMN("NOT_EXIST_SENSOR_VALUE_COLUMN",
      "神策事件节点中 identities 中 sensorIdValueColumn 对应的值不存在于 columns 里面."),
  NOT_EXIST_ITEM_ID_COLUMN("NOT_EXIST_SENSOR_VALUE_COLUMN", "神策事件节点中 itemIdColumn 对应的值不存在于 columns 里面."),
  NOT_EXIST_ITEM_TYPE_COLUMN("NOT_EXIST_ITEM_TYPE_COLUMN", "神策事件节点中 itemValueColumn 对应的值不存在于 columns 里面."),
  NOT_EXIST_DISTINCT_ID_COLUMN("NOT_EXIST_DISTINCT_ID_COLUMN", "神策事件节点中 distinctIdColumn 对应的值不存在于 columns 里面."),
  NOT_EXIST_EVENT_NAME_COLUMN("NOT_EXIST_EVENT_NAME_COLUMN", "神策事件节点中 eventNameColumn 对应的值不存在于 columns 里面."),
  NOT_EXIST_FILE_PATH("NOT_EXIST_FILE_PATH", "配置的 sdk 节点 logPathAddress 文件夹路径不存在。"),
  ERROR_SDK_URI("ERROR_SDK_URI", "输入的 SDK uri 不合法！"),
  NONE_KEY_SDK("NONE_KEY_SDK", "writer 配置文件中不存在 sdk 节点！");

  public String key;
  public String value;

  SaErrorEnum(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String getCode() {
    return key;
  }

  @Override
  public String getDescription() {
    return value;
  }
}