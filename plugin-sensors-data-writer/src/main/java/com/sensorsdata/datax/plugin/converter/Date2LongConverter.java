package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.common.SensorsColumn;
import com.sensorsdata.datax.plugin.convert.Converter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Date to Long;若来源不是 date 类型，则使用当前时间戳
 */
public class Date2LongConverter implements Converter {


    @Override
    public SensorsColumn transform(SensorsColumn column, Map<String, Object> param) {
        Object value = column.getColumnData();
        if (Objects.isNull(value)) {
            return new SensorsColumn(SensorsColumn.SensorsType.NUMBER, null);
        }
        SensorsColumn.SensorsType type = column.getType();
        switch (type) {
            case DATE:
                return new SensorsColumn(SensorsColumn.SensorsType.NUMBER, ((Date) value).getTime());
            case NUMBER:
                return column;
            default:
                return new SensorsColumn(SensorsColumn.SensorsType.NUMBER, new Date().getTime());
        }
    }
}
