package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.analytics.javasdk.bean.ItemRecord;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.datax.plugin.exception.SaErrorEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorsItem extends AbstractSensorData {
  /**
   * itemId 对应的 column 值
   */
  private String itemIdColumn;
  /**
   * itemType 对应的 column 值
   */
  private String itemTypeColumn;
  /**
   * itemType 固定值
   */
  private String itemType;

  public CheckResult check(List<SensorColumn> columns) {
    if (StringUtils.isBlank(itemIdColumn)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_ITEM_ID).build();
    }
    if (StringUtils.isBlank(itemTypeColumn) && StringUtils.isBlank(itemType)) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NONE_KEY_EVENT_TYPE).build();
    }
    boolean b = columns.stream().anyMatch(it -> StringUtils.equals(itemIdColumn, it.getName()));
    if (!b) {
      return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_ITEM_ID_COLUMN).build();
    }
    if (StringUtils.isNotBlank(itemTypeColumn)) {
      boolean b1 = columns.stream().anyMatch(it -> StringUtils.equals(itemTypeColumn, it.getName()));
      if (!b1) {
        return CheckResult.builder().errorEnum(SaErrorEnum.NOT_EXIST_ITEM_TYPE_COLUMN).build();
      }
    }
    return CheckResult.builder().success(true).build();
  }

  @Override
  public ItemRecord convertItemRecord(Map<String, Object> fieldsMap) throws InvalidArgumentException {
    String itemId = fieldsMap.remove(itemIdColumn).toString();
    String itemTypeValue =
        StringUtils.isNoneBlank(itemTypeColumn) ? fieldsMap.remove(itemTypeColumn).toString() : itemType;
    return ItemRecord.builder()
        .setItemId(itemId)
        .setItemType(itemTypeValue)
        .addProperties(fieldsMap)
        .build();
  }

}
