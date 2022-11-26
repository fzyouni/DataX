package com.sensorsdata.datax.plugin.util;


import com.sensorsdata.datax.plugin.bean.DataConverter;
import com.sensorsdata.datax.plugin.bean.SensorColumn;
import com.sensorsdata.datax.plugin.convert.Converter;

import com.alibaba.datax.common.element.Column;

import java.util.List;
import java.util.Objects;

public class ConverterUtil {


    public static Object convertField(SensorColumn sensorColumn, Column column) {
        Object res = column2Obj(column);
        if (Objects.isNull(res)) {
            return null;
        }
        List<DataConverter> dataConverters = sensorColumn.getDataConverters();
        if (dataConverters == null) {
            return res;
        }
        for (DataConverter dataConverter : dataConverters) {
            Converter converter = dataConverter.getConverter();
            if (Objects.isNull(converter)) {
                continue;
            }
            res = converter.transform(res, dataConverter.getParam());
        }
        return res;
    }

    private static Object column2Obj(Column column) {
        Column.Type type = column.getType();
        switch (type) {
            case STRING:
                return column.asString();
            case BYTES:
                return column.asBytes();
            case LONG:
                return column.asLong();
            case DATE:
                return column.asDate();
            case INT:
                return column.asBigInteger();
            case DOUBLE:
                return column.asDouble();
            case BOOL:
                return column.asBoolean();
            case BAD:
            case NULL:
            default:
                return null;
        }
    }
}
