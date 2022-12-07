package com.sensorsdata.datax.plugin.convert;

import com.sensorsdata.datax.plugin.converter.Date2LongConverter;
import com.sensorsdata.datax.plugin.converter.Date2StrConverter;
import com.sensorsdata.datax.plugin.converter.IfNullConverter;
import com.sensorsdata.datax.plugin.converter.Long2DateConverter;
import com.sensorsdata.datax.plugin.converter.Split2ListConverter;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConverterFactory {

    private static final Map<String, Converter> converterMap = new HashMap<>();

    static {
        converterMap.put("Long2Date", new Long2DateConverter());
        converterMap.put("Date2Str", new Date2StrConverter());
        converterMap.put("Date2Long", new Date2LongConverter());
        converterMap.put("IfNull", new IfNullConverter());
        converterMap.put("Split2List", new Split2ListConverter());
    }

    public static Converter converter(String type) {
        if (StrUtil.isEmpty(type)) {
            return null;
        }
        return converterMap.get(type);
    }

    /**
     * 获取多例的转换器
     *
     * @param type 转换器名称
     * @return 转换器
     */
    public static Converter converterPrototype(String type) {
        if (StrUtil.isBlank(type) || Objects.isNull(converterMap.get(type))) {
            return null;
        }
        try {
            return converterMap.get(type).getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
