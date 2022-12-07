package com.sensorsdata.datax.plugin.util;

import com.sensorsdata.datax.plugin.bean.CheckResult;
import com.sensorsdata.datax.plugin.bean.SensorColumn;
import com.sensorsdata.datax.plugin.bean.SensorsItem;
import com.sensorsdata.datax.plugin.bean.SensorsProfile;
import com.sensorsdata.datax.plugin.bean.SensorsSDKConfig;
import com.sensorsdata.datax.plugin.bean.SensorsTrack;
import com.sensorsdata.datax.plugin.bean.TypeEnum;
import com.sensorsdata.datax.plugin.exception.SaErrorEnum;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/11/23 17:38
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUtils {


  public static CheckResult checkWriteConfig(Configuration config) {
    if (config.get(ConfigConstant.TYPE) == null) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_EVENT_TYPE).build();
    }
    if (config.get(ConfigConstant.SDK) == null) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_SDK).build();
    }
    SensorsSDKConfig sensorsSDKConfig =
        JSONObject.parseObject(config.getString(ConfigConstant.SDK), SensorsSDKConfig.class);
    CheckResult check = sensorsSDKConfig.check();
    if (!check.getSuccess()) {
      return check;
    }
    if (config.get(ConfigConstant.COLUMN) == null) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_COLUMNS).build();
    }
    List<SensorColumn> columns = JSONArray.parseArray(config.getString(ConfigConstant.COLUMN), SensorColumn.class);
    if (columns == null || columns.size() == 0) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_COLUMNS).build();
    }
    Optional<CheckResult> first =
        columns.stream().map(SensorColumn::check).filter(it -> !it.getSuccess()).findFirst();
    if (first.isPresent()) {
      return first.get();
    }
    TypeEnum typeEnum = TypeEnum.getEventTypeByName(config.getString(ConfigConstant.TYPE));
    switch (typeEnum) {
      case EVENT:
        if (config.get(ConfigConstant.TRACK) == null) {
          return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_TRACK).build();
        }
        SensorsTrack sensorsTrack = JSONObject.parseObject(config.getString(ConfigConstant.TRACK), SensorsTrack.class);
        return sensorsTrack.check(columns);
      case USER:
        if (config.get(ConfigConstant.PROFILE_SET) == null) {
          return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_PROFILE_SET).build();
        }
        SensorsProfile sensorsProfile =
            JSONObject.parseObject(config.getString(ConfigConstant.PROFILE_SET), SensorsProfile.class);
        return sensorsProfile.check(columns);
      case ITEM:
        if (config.get(ConfigConstant.ITEM_SET) == null) {
          return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_ITEM_SET).build();
        }
        SensorsItem sensorsItem = JSONObject.parseObject(config.getString(ConfigConstant.ITEM_SET), SensorsItem.class);
        return sensorsItem.check(columns);
      case UNKNOWN:
        return CheckResult.builder().errorEnum(SaErrorEnum.UNKNOWN_EVENT_TYPE).build();
    }
    return CheckResult.builder().success(true).build();


  }

}
