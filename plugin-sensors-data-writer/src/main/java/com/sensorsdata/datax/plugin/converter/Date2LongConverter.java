package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.convert.Converter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Date to Long;若来源不是 date 类型，则使用当前时间戳
 */
public class Date2LongConverter implements Converter {


    @Override
    public Object transform(Object value, Map<String, Object> param) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Long) {
            return value;
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        return new Date().getTime();
    }
}
