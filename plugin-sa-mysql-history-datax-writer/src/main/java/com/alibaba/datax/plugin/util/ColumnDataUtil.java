package com.alibaba.datax.plugin.util;

import cn.hutool.core.convert.Convert;
import com.alibaba.datax.plugin.domain.TableColumnMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ColumnDataUtil {

    private ColumnDataUtil(){}
    static {
        DateUtil.registerFormat("HH:mm:ss");
        DateUtil.registerFormat("HHmmss");
    }

    public static String transformInsertBatchSql(String model,String tableName, List<String> tableColumnOrderList, Map<String, TableColumnMetaData> tableColumnMetaDataMap, List<Map<String,Object>> propertiesList){
        StringBuilder sb = new StringBuilder().append(model).append(" INTO ").append(tableName).append("(")
                .append(tableColumnOrderList.stream().collect(Collectors.joining(","))).append(") VALUES ");
        StringBuilder sbTemp;
        for (Map<String, Object> properties : propertiesList) {
            sbTemp = new StringBuilder("(");
            int nullCount = 0;
            for (int i = 0; i < tableColumnOrderList.size(); i++) {
                String columnName = tableColumnOrderList.get(i);
                TableColumnMetaData tableColumnMetaData = tableColumnMetaDataMap.get(columnName);
                String columnValue = generateColumnValue(columnName,tableColumnMetaData,properties.get(columnName));
                if(Objects.isNull(columnValue) || "null".equalsIgnoreCase(columnValue)){
                    nullCount ++;
                }
                sbTemp.append(columnValue).append(",");
            }
            sbTemp.deleteCharAt(sbTemp.length() - 1);
            sbTemp.append("),");
            if(nullCount != tableColumnOrderList.size()){
                sb.append(sbTemp.toString());
            }
        }
        if(sb.toString().endsWith(") VALUES ")){
//            这里说明没有一个要插入的属性值
            return null;
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String transformInsertSql(String model,String tableName, List<String> tableColumnOrderList, Map<String, TableColumnMetaData> tableColumnMetaDataMap, Map<String,Object> properties){
        StringBuilder sb = new StringBuilder().append(model).append(" INTO ").append(tableName).append("(")
                .append(tableColumnOrderList.stream().collect(Collectors.joining(","))).append(") VALUES (");
        int nullCount = 0;
        for (int i = 0; i < tableColumnOrderList.size(); i++) {
            String columnName = tableColumnOrderList.get(i);
            TableColumnMetaData tableColumnMetaData = tableColumnMetaDataMap.get(columnName);
            String columnValue = generateColumnValue(columnName,tableColumnMetaData,properties.get(columnName));
            if(Objects.isNull(columnValue) || "null".equalsIgnoreCase(columnValue)){
                nullCount ++;
            }
            sb.append(columnValue).append(",");
        }
        if(nullCount == tableColumnOrderList.size()){
//            这里说明没有一个要插入的属性值
            return null;
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    public static String transformUpdateSql(String model,String tableName, List<String> tableColumnOrderList,
                                            Map<String, TableColumnMetaData> tableColumnMetaDataMap,
                                            List<String> updateWhereColumn, Map<String, Object> properties,
                                            List<String> insertUpdateModelNotUpdateColumnList,boolean nullValueIsUpdate,Map<String, String> updateNewValueColMap) {
        StringBuilder sb = new StringBuilder().append(model).append(" ").append(tableName).append(" SET ");
        for (String columnName : tableColumnOrderList) {
            if(( updateWhereColumn.contains(columnName) || Objects.isNull(tableColumnMetaDataMap.get(columnName)) ||
                    insertUpdateModelNotUpdateColumnList.contains(columnName) ) ||
                ( !nullValueIsUpdate && Objects.isNull(properties.get(columnName)) ) ){
                continue;
            }
            TableColumnMetaData tableColumnMetaData = tableColumnMetaDataMap.get(columnName);
            String columnValue = generateColumnValue(columnName, tableColumnMetaData, properties.get(columnName));
            sb.append(columnName).append(" = ").append(columnValue).append(",");
        }
        if(Objects.nonNull(updateNewValueColMap) && !updateNewValueColMap.isEmpty()){
            updateNewValueColMap.forEach((k,v)->{
                TableColumnMetaData tableColumnMetaData = tableColumnMetaDataMap.get(k);
                String columnValue = generateColumnValue(k, tableColumnMetaData, properties.get(v));
                sb.append(k).append(" = ").append(columnValue).append(",");
            });
        }
        if(sb.toString().endsWith(" SET ")){
//            这里说明没有一个要修改的属性值
            return null;
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE ");
        for (String columnName : updateWhereColumn) {
            Object value = properties.get(columnName);
            TableColumnMetaData tableColumnMetaData = tableColumnMetaDataMap.get(columnName);
            if(Objects.isNull(value) || Objects.isNull(tableColumnMetaData)){
                sb.append(columnName).append(" is null and ");
            }else{
                String columnValue = generateColumnValue(columnName, tableColumnMetaData, value);
                sb.append(columnName).append(" = ").append(columnValue).append(" and ");
            }
        }
        sb.delete(sb.length() - 4,sb.length());
        return sb.toString();
    }

    private static String generateColumnValue(String columnName, TableColumnMetaData tableColumnMetaData, Object value) {
        if(Objects.isNull(value) || Objects.isNull(tableColumnMetaData) || Objects.isNull(tableColumnMetaData.getType())
         || Objects.equals("",tableColumnMetaData.getType())){
            return "null";
        }
        int type = tableColumnMetaData.getType();
        switch (type) {
            case Types.CHAR:
            case Types.NCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.BLOB:
            case Types.LONGVARBINARY:
                if(value instanceof byte[]){
                    char[] chs = Hex.encodeHex((byte[]) value);
                    return "0x" + new String(chs);
                }
                return "'" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'";
            case Types.BINARY:
            case Types.VARBINARY:
                byte[] v = null;
                if(value instanceof String){
                    v = ((String)value).getBytes();
                }else{
                    v = (byte[]) value;
                }
                char[] chars = Hex.encodeHex(v);
                return "0x" + new String(chars);
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.TINYINT:
            case Types.BOOLEAN:
            case Types.BIT:
                if(value instanceof byte[]){
                    char[] chs = Hex.encodeHex((byte[]) value);
                    return "0x" + new String(chs);
                }
                return value.toString();
            case Types.TIMESTAMP:
//                datetime or timestamp
                Date d = Convert.convert(Date.class, value);
                if(Objects.isNull(d)){
                    return "'" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'";
                }
                return "'" + DateUtil.date2Str(d) + "'";
            case Types.DATE:
                Date d1 = Convert.convert(Date.class, value);
                if(Objects.isNull(d1)){
                    return "'" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'";
                }
                if("year".equalsIgnoreCase(tableColumnMetaData.getTypeName())){
                    return "'" + DateUtil.date2Str(d1,"yyyy") + "'";
                }
                return "'" + DateUtil.date2Str(d1,"yyyy-MM-dd") + "'";
            case Types.TIME:
                Date d2 = Convert.convert(Date.class, value);
                if(Objects.isNull(d2)){
                    return "'" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'";
                }
                return "'" + DateUtil.date2Str(d2,"HH:mm:ss") + "'";

        }
        log.info("暂不支持的数据类型，mysql type:{},value:{},value type:{}",tableColumnMetaData.getTypeName(),value,Objects.isNull(value)?"null":value.getClass());
        return "'" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'";
    }


}
