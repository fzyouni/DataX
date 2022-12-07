package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.common.SensorsColumn;
import com.sensorsdata.datax.plugin.convert.Converter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Long2DateConverter implements Converter {


    @Override
    public SensorsColumn transform(SensorsColumn column, Map<String, Object> param) {
        Object value = column.getColumnData();
        if (Objects.isNull(value)) {
            return new SensorsColumn();
        }
        switch (column.getType()) {
            case NUMBER:
                return new SensorsColumn(SensorsColumn.SensorsType.DATE, new Date(Long.parseLong(value.toString())));
            case DATE:
                return column;
            default:
                return new SensorsColumn(SensorsColumn.SensorsType.DATE, new Date());

        }
    }
}
