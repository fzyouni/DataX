package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.common.SensorsColumn;
import com.sensorsdata.datax.plugin.convert.Converter;

import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Date2StrConverter implements Converter {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public SensorsColumn transform(SensorsColumn column, Map<String, Object> param) {
        Object value = column.getColumnData();
        if (Objects.isNull(value)) {
            return null;
        }
        String pattern = param.getOrDefault("pattern", DEFAULT_PATTERN).toString();
        switch (column.getType()) {
            case DATE:
                return new SensorsColumn(SensorsColumn.SensorsType.STRING, DateUtil.format((Date) value, pattern));
            case STRING:
                return column;
            default:
                return new SensorsColumn(SensorsColumn.SensorsType.STRING, DateUtil.format(new Date(), pattern));
        }
    }
}
