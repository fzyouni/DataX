package com.sensorsdata.datax.plugin.convert;

import java.util.Map;

public interface Converter {

     Object transform(Object value, Map<String, Object> param);
}
