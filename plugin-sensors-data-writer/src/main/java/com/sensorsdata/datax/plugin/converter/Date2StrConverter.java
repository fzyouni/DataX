package com.sensorsdata.datax.plugin.converter;

import com.sensorsdata.datax.plugin.convert.Converter;

import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Date2StrConverter implements Converter {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Object transform(Object value, Map<String, Object> param) {
        if (Objects.isNull(value)) {
            return null;
        }
        String pattern = param.getOrDefault("pattern", DEFAULT_PATTERN).toString();
        Date date;
        if (value instanceof Date) {
            date = (Date) value;
        } else {
            date = new Date();
        }
        return DateUtil.format(date, pattern);
    }
}
