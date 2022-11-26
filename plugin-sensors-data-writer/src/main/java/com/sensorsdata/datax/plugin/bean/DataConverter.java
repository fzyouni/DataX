package com.sensorsdata.datax.plugin.bean;

import com.sensorsdata.datax.plugin.convert.Converter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataConverter implements Serializable {

    private static final long serialVersionUID = 1829286600375132047L;

    private String type;

    private Map<String, Object> param;
}
