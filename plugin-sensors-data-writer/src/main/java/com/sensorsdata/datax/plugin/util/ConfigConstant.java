package com.sensorsdata.datax.plugin.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigConstant {

  // 写插件必传参数
  public static final String TYPE = "type";
  public static final String COLUMN = "columns";

  // 二选一
  public static final String SDK_NET_ADDRESS = "sdkNetAddress";
  public static final String SDK_LOG_PATH_ADDRESS = "sdkLogPathAddress";

  //生成 神策 JSON 结构参数
  // N 选一,表示神策事件类型
  public static final String TRACK = "track";
  public static final String PROFILE_SET = "profileSet";
  public static final String ITEM_SET = "itemSet";


}
