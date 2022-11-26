package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.convert.Converter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Long2DateConverter implements Converter {

    @Override
    public Object transform(Object value, Map<String, Object> param) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Long) {
            return new Date(Long.parseLong(value.toString()));
        }
        if (value instanceof Date) {
            return value;
        }
        return new Date();
    }
}
