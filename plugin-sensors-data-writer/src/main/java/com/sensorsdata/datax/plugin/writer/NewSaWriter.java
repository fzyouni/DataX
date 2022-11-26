package com.sensorsdata.datax.plugin.writer;

import com.sensorsdata.analytics.javasdk.bean.EventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMEventRecord;
import com.sensorsdata.analytics.javasdk.bean.IDMUserRecord;
import com.sensorsdata.analytics.javasdk.bean.ItemRecord;
import com.sensorsdata.analytics.javasdk.bean.UserRecord;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.datax.plugin.bean.CheckResult;
import com.sensorsdata.datax.plugin.bean.SensorColumn;
import com.sensorsdata.datax.plugin.bean.SensorData;
import com.sensorsdata.datax.plugin.bean.SensorsItem;
import com.sensorsdata.datax.plugin.bean.SensorsProfile;
import com.sensorsdata.datax.plugin.bean.SensorsTrack;
import com.sensorsdata.datax.plugin.bean.TypeEnum;
import com.sensorsdata.datax.plugin.exception.SaDataXException;
import com.sensorsdata.datax.plugin.util.ConfigConstant;
import com.sensorsdata.datax.plugin.util.ConfigUtils;
import com.sensorsdata.datax.plugin.util.ConverterUtil;
import com.sensorsdata.datax.plugin.util.RecordCountUtil;
import com.sensorsdata.datax.plugin.util.SaSdkFactory;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class NewSaWriter extends Writer {

  public static class Job extends Writer.Job {
    private Configuration originalConfig;

    @Override
    public void init() {
      this.originalConfig = super.getPluginJobConf();
      CheckResult checkResult = ConfigUtils.checkWriteConfig(originalConfig);
      if (!checkResult.getSuccess()) {
        throw new SaDataXException(checkResult.getErrorEnum());
      }
      String netAddress = originalConfig.getString(ConfigConstant.SDK_NET_ADDRESS);
      if (StringUtils.isNotBlank(netAddress)) {
        SaSdkFactory.SELF.initSdkNetAddress(netAddress);
      }
      String logAddress = originalConfig.getString(ConfigConstant.SDK_LOG_PATH_ADDRESS);
      if (StringUtils.isNotBlank(logAddress)) {
        SaSdkFactory.SELF.initSdkLogAddress(logAddress);
      }
      List<SensorColumn> columns =
          JSONArray.parseArray(originalConfig.getString(ConfigConstant.COLUMN), SensorColumn.class);
      columns.forEach(
          it -> {
            if (it.getIfNullGiveUp()) {
              RecordCountUtil.initNullGiveUpCount(it.getName());
            }
          }
      );
    }

    @Override
    public void destroy() {
      SaSdkFactory.SELF.close();
    }

    @Override
    public List<Configuration> split(int number) {
      List<Configuration> objects = new ArrayList<>(number);
      for (int i = 0; i < number; i++) {
        objects.add(this.originalConfig.clone());
      }
      return objects;
    }


    @Override
    public void post() {
      super.post();
      Map<String, AtomicLong> recordMap = RecordCountUtil.getRecordMap();
      Map<String, AtomicLong> nullCountMap = RecordCountUtil.getNullCountMap();
      Map<String, AtomicLong> nullGiveUpCountMap = RecordCountUtil.getNullGiveUpCountMap();
      recordMap.forEach((key, value) -> log.info("本次任务执行结束，一共发送 {} 类型 {} 条数据；", key, value));
      nullCountMap.forEach((key, value) -> log.info("因 column 值为空，抛弃字段属性，统计 column：{},空值出现次数：{}", key, value));
      nullGiveUpCountMap.forEach((key, value) -> log.info("因 column 值为空，抛弃整条记录，统计 column：{},空值出现次数：{}", key, value));
    }
  }


  public static class Task extends Writer.Task {

    private List<SensorColumn> columns;

    private Pair<TypeEnum, SensorData> pair;

    @Override
    public void init() {
      Configuration writerConfig = super.getPluginJobConf();
      String type = writerConfig.getString(ConfigConstant.TYPE);
      columns = JSONArray.parseArray(writerConfig.getString(ConfigConstant.COLUMN), SensorColumn.class);
      TypeEnum typeEnum = TypeEnum.getEventTypeByName(type);
      switch (typeEnum) {
        case EVENT:
          SensorsTrack sensorsTrack =
              JSONObject.parseObject(writerConfig.getString(ConfigConstant.TRACK), SensorsTrack.class);
          pair = Pair.of(typeEnum, sensorsTrack);
          break;
        case USER:
          SensorsProfile sensorsProfile =
              JSONObject.parseObject(writerConfig.getString(ConfigConstant.PROFILE_SET), SensorsProfile.class);
          pair = Pair.of(typeEnum, sensorsProfile);
          break;
        case ITEM:
          SensorsItem sensorsItem =
              JSONObject.parseObject(writerConfig.getString(ConfigConstant.ITEM_SET), SensorsItem.class);
          pair = Pair.of(typeEnum, sensorsItem);
          break;
        default:
          break;
      }
    }

    @Override
    public void startWrite(RecordReceiver lineReceiver) {
      Record record;
      while ((record = lineReceiver.getFromReader()) != null) {
        Map<String, Object> fieldsMap = new HashMap<>();
        boolean sendFlag = true;
        for (SensorColumn sensorColumn : columns) {
          Column column = record.getColumn(sensorColumn.getIndex());
          Object value = ConverterUtil.convertField(sensorColumn, column);
          if (Objects.isNull(value)) {
            if (sensorColumn.getIfNullGiveUp()) {
              sendFlag = false;
              RecordCountUtil.incNullGiveUpCount(sensorColumn.getName());
              break;
            } else {
              RecordCountUtil.incExcludeCount(sensorColumn.getName());
              continue;
            }
          }
          fieldsMap.put(sensorColumn.getName(), value);
        }
        try {
          if (sendFlag) {
            convertFieldsMappingAndSend(pair, fieldsMap);
          }
        } catch (InvalidArgumentException e) {
          log.error("generate sensors data json fail.", e);
          RecordCountUtil.incBadRecordCount();
        }
      }
    }

    @Override
    public void destroy() {

    }

    private void convertFieldsMappingAndSend(Pair<TypeEnum, SensorData> pair, Map<String, Object> fieldsMap)
        throws InvalidArgumentException {
      TypeEnum type = pair.getKey();
      SensorData sensorData = pair.getValue();
      switch (type) {
        case EVENT:
          if (sensorData.isIdm3()) {
            IDMEventRecord eventRecord = sensorData.convertEventRecord3(fieldsMap);
            SaSdkFactory.SELF.send(eventRecord);
          } else {
            EventRecord eventRecord = sensorData.convertEventRecord(fieldsMap);
            SaSdkFactory.SELF.send(eventRecord);
          }
          RecordCountUtil.incRecordCount(type.name());
          break;
        case USER:
          if (sensorData.isIdm3()) {
            IDMUserRecord userRecord = sensorData.convertUserRecord3(fieldsMap);
            SaSdkFactory.SELF.send(userRecord);
          } else {
            UserRecord userRecord = sensorData.convertUserRecord(fieldsMap);
            SaSdkFactory.SELF.send(userRecord);
          }
          RecordCountUtil.incRecordCount(type.name());
          break;
        case ITEM:
          ItemRecord record = sensorData.convertItemRecord(fieldsMap);
          SaSdkFactory.SELF.send(record);
          RecordCountUtil.incRecordCount(type.name());
          break;
        default:
          //do nothing
          break;
      }
    }

  }
}
