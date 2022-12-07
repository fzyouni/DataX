package com.sensorsdata.datax.plugin.convert;

import com.sensorsdata.datax.plugin.common.SensorsColumn;

import java.util.Map;

public interface Converter {

     SensorsColumn transform(SensorsColumn column, Map<String, Object> param);
}
