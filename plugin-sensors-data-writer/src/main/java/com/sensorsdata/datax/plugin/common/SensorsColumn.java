package com.sensorsdata.datax.plugin.common;

import com.alibaba.datax.common.element.Column;
import lombok.Data;



@Data
public class SensorsColumn {

  private SensorsType type;

  private Object columnData;

  public SensorsColumn() {
    this.type = SensorsType.UNKNOWN;
    this.columnData = null;
  }

  public SensorsColumn(Column column) {
    this.type = SensorsType.getType(column);
    this.columnData = column.getRawData();
  }

  public SensorsColumn(SensorsType type, Object value) {
    this.type = type;
    this.columnData = value;
  }

  public enum SensorsType {

    NUMBER,
    STRING,
    DATE,
    LIST,
    BOOLEAN,
    UNKNOWN;


    public static SensorsType getType(Column column) {
      Column.Type type = column.getType();
      switch (type) {
        case INT:
        case LONG:
        case DOUBLE:
          return NUMBER;
        case DATE:
          return DATE;
        case BOOL:
          return BOOLEAN;
        case STRING:
          return STRING;
        default:
          return UNKNOWN;
      }
    }
  }
}
