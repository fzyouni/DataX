package com.alibaba.datax.plugin.writer;

import cn.hutool.db.handler.BeanListHandler;
import cn.hutool.db.sql.SqlExecutor;
import com.alibaba.datax.BasePlugin;
import com.alibaba.datax.common.element.*;
import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.ConverterFactory;
import com.alibaba.datax.plugin.KeyConstant;
import com.alibaba.datax.plugin.classloader.PluginClassLoader;
import com.alibaba.datax.plugin.domain.*;
import com.alibaba.datax.plugin.util.ColumnDataUtil;
import com.alibaba.datax.plugin.util.ConverterUtil;
import com.alibaba.datax.plugin.util.ImpalaUtil;
import com.alibaba.datax.plugin.util.NullUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class SaWriter extends Writer {

    @Slf4j
    public static class Job extends Writer.Job{

        private Configuration originalConfig = null;

        public List<Configuration> split(int i) {
            List<Configuration> list = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                list.add(this.originalConfig.clone());
            }
            return list;
        }

        public void init() {
            this.originalConfig = super.getPluginJobConf();
            String url = originalConfig.getString(KeyConstant.URL);
            String table = originalConfig.getString(KeyConstant.TABLE);
            if(Objects.isNull(url) || Objects.equals("",url) ){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"url不应该为空！");
            }
            if(Objects.isNull(table) || Objects.equals("",table) ){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"table不应该为空！");
            }
            String model = originalConfig.getString(KeyConstant.MODEL);
            if(Objects.isNull(model) || Objects.equals("",model)){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"model不应该为空！可选值:insert/insertBatch/update/insertUpdate/upsert/upsertBatch");
            }
            if(!(Objects.equals("insert",model) || Objects.equals("insertBatch",model)
                    || Objects.equals("update",model) || Objects.equals("insertUpdate",model)
                    || Objects.equals("upsert",model) || Objects.equals("upsertBatch",model))
            ){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"model值不正确！可选值:insert/insertBatch/update/insertUpdate/upsert/upsertBatch");
            }
            if((Objects.equals("update",model) || Objects.equals("insertUpdate",model))
               && originalConfig.getList(KeyConstant.UPDATE_WHERE_COLUMN,new ArrayList<>()).isEmpty()){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"model为update或者insertUpdate时，updateWhereColumn不能为空！");
            }
            String userName = originalConfig.getString(KeyConstant.USER_NAME,"");
            String password = originalConfig.getString(KeyConstant.PASSWORD,"");
            ImpalaUtil.setUrl(url);
            ImpalaUtil.setUser(userName);
            ImpalaUtil.setPassword(password);
            DataSource dataSource = ImpalaUtil.defaultDataSource();
            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;
            try {
                connection = dataSource.getConnection();

                String queryColumnSql = "select * from " + table + " where 1=2";
                List<TableColumnMetaData> tableColumnMetaDataList = new ArrayList<>();
                statement = connection.createStatement();
                rs = statement.executeQuery(queryColumnSql);
                ResultSetMetaData rsMetaData = rs.getMetaData();
                for (int i = 0, len = rsMetaData.getColumnCount(); i < len; i++) {
                    TableColumnMetaData meta = new TableColumnMetaData();
                    meta.setName(rsMetaData.getColumnName(i + 1));
                    meta.setType(rsMetaData.getColumnTypeName(i + 1));
                    meta.setTypeIndex(rsMetaData.getColumnType(i + 1));
                    tableColumnMetaDataList.add(meta);
                }
                if(Objects.isNull(tableColumnMetaDataList) || tableColumnMetaDataList.isEmpty()){
                    throw new DataXException(CommonErrorCode.CONFIG_ERROR,"获取表["+table+"]列元数据为空，请先添加列.");
                }

                originalConfig.set(KeyConstant.TABLE_COLUMN_META_DATA,JSONObject.toJSONString(tableColumnMetaDataList));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                log.error("获取表[{}]列元数据时发生错误：",table,throwables);
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"获取表["+table+"]列元数据时发生错误.");
            }finally {
                if(Objects.nonNull(rs)){
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if(Objects.nonNull(statement)){
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if(Objects.nonNull(connection)){
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            JSONArray saColumnJsonArray = originalConfig.get(KeyConstant.SA_COLUMN, JSONArray.class);
            if(Objects.isNull(saColumnJsonArray)){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"column不应该为空！");
            }
            String saColumnStr = saColumnJsonArray.toJSONString();
            List<SaColumnItem> saColumnList = JSONObject.parseArray(saColumnStr, SaColumnItem.class);
            if(Objects.isNull(saColumnList) || saColumnList.isEmpty()){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"column不应该为空！");
            }

        }

        public void destroy() {}
    }

    @Slf4j
    public static class Task extends Writer.Task{

        private Configuration readerConfig;

        private List<SaColumnItem> saColumnList;

        private Map<String, TableColumnMetaData> tableColumnMetaDataMap;

        private List<String> tableColumnOrderList = new ArrayList<>();

        private List<BasePlugin.SAPlugin> basePluginList;

        private String tableName;
        private String model;
        private int batchSize;
        private List<String> updateWhereColumn;


        public void startWrite(RecordReceiver recordReceiver) {
            Record record = null;
            List<Map<String,Object>> batchList = new ArrayList<>(this.batchSize);

            A:while((record = recordReceiver.getFromReader()) != null) {
                Map<String,Object> properties = new HashMap<>();

                for (SaColumnItem col : saColumnList) {
                    Column column = record.getColumn(col.getIndex());
                    if(Objects.isNull(column)){
                        if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                            continue A;
                        }
                        continue;
                    }else if(column instanceof StringColumn){
                        String v = column.asString();
                        Object value = ConverterUtil.convert(col.getName(),v,col,properties);
                        if(NullUtil.isNullOrBlank(value)){
                            if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                                continue A;
                            }
                            continue;
                        }
                        properties.put(col.getName(),value);
                    }else if(column instanceof BoolColumn){
                        Boolean v = column.asBoolean();
                        Object value = ConverterUtil.convert(col.getName(),v,col,properties);
                        if(NullUtil.isNullOrBlank(value)){
                            if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                                continue A;
                            }
                            continue;
                        }
                        properties.put(col.getName(),value);
                    }else if(column instanceof DoubleColumn){
                        BigDecimal v = column.asBigDecimal();
                        Object value = ConverterUtil.convert(col.getName(),v,col,properties);
                        if(NullUtil.isNullOrBlank(value)){
                            if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                                continue A;
                            }
                            continue;
                        }
                        properties.put(col.getName(),value);
                    }else if(column instanceof LongColumn){
                        BigInteger v = column.asBigInteger();
                        Object value = ConverterUtil.convert(col.getName(),v,col,properties);
                        if(NullUtil.isNullOrBlank(value)){
                            if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                                continue A;
                            }
                            continue;
                        }
                        properties.put(col.getName(),value);
                    }else if(column instanceof DateColumn){
                        Date v = column.asDate();
                        Object value = ConverterUtil.convert(col.getName(),v,col,properties);
                        if(NullUtil.isNullOrBlank(value)){
                            if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                                continue A;
                            }
                            continue;
                        }
                        properties.put(col.getName(),value);
                    }else if(column instanceof BytesColumn){
                        byte[] v = column.asBytes();
                        Object value = ConverterUtil.convert(col.getName(),v,col,properties);
                        if(NullUtil.isNullOrBlank(value)){
                            if(!NullUtil.isNullOrBlank(col.getIfNullGiveUp()) && col.getIfNullGiveUp()){
                                continue A;
                            }
                            continue;
                        }
                        properties.put(col.getName(),value);
                    }
                }
                boolean process = true;
                if(!Objects.isNull(this.basePluginList) && !this.basePluginList.isEmpty()){
                    for (BasePlugin.SAPlugin saPlugin : this.basePluginList) {
                        process = saPlugin.process(properties);
                        if(!process){
                            continue A;
                        }
                    }
                }
                String sql = generateSql(tableName, tableColumnOrderList, this.tableColumnMetaDataMap, properties, batchList);
                if(Objects.isNull(sql) || Objects.equals("",sql)){
                    continue;
                }
                insertData(sql,tableName,tableColumnOrderList,this.tableColumnMetaDataMap,properties,batchList);
                sql = null;
            }
            if((Objects.equals("insertBatch",this.model) || Objects.equals("upsertBatch",this.model)) && !batchList.isEmpty()){
                String model = "INSERT";
                if(Objects.equals("upsertBatch",this.model)){
                    model = "UPSERT";
                }
                String sql = ColumnDataUtil.transformInsertBatchSql(model,tableName,tableColumnOrderList,this.tableColumnMetaDataMap,batchList);
                if(!(Objects.isNull(sql) || Objects.equals("",sql))){
                    executeSql(sql);
                }
                batchList.clear();
                sql = null;
                model = null;
            }
        }

        private String generateSql(String tableName, List<String> tableColumnOrderList, Map<String, TableColumnMetaData> tableColumnMetaDataMap,
                                   Map<String, Object> properties, List<Map<String, Object>> batchList){
            String sql = null;
            if(Objects.equals("insert",this.model)){
                sql = ColumnDataUtil.transformInsertSql("INSERT",tableName,tableColumnOrderList,this.tableColumnMetaDataMap,properties);
            } else if(Objects.equals("insertBatch",this.model)){
                batchList.add(properties);
                if(this.batchSize == batchList.size()){
                    sql = ColumnDataUtil.transformInsertBatchSql("INSERT",tableName,tableColumnOrderList,this.tableColumnMetaDataMap,batchList);
                }
            }else if(Objects.equals("update",this.model)){
                sql = ColumnDataUtil.transformUpdateSql(tableName,tableColumnOrderList,this.tableColumnMetaDataMap,this.updateWhereColumn,properties);
            }else if(Objects.equals("insertUpdate",this.model)){
                sql = ColumnDataUtil.transformInsertSql("INSERT",tableName,tableColumnOrderList,this.tableColumnMetaDataMap,properties);
            }else if(Objects.equals("upsert",this.model)){
                sql = ColumnDataUtil.transformInsertSql("UPSERT",tableName,tableColumnOrderList,this.tableColumnMetaDataMap,properties);
            }else if(Objects.equals("upsertBatch",this.model)){
                batchList.add(properties);
                if(this.batchSize == batchList.size()){
                    sql = ColumnDataUtil.transformInsertBatchSql("UPSERT",tableName,tableColumnOrderList,this.tableColumnMetaDataMap,batchList);
                }
            }else{
                log.info("不支持的模式：{}",this.model);
            }
            return sql;
        }

        private void insertData(String sql,String tableName, List<String> tableColumnOrderList, Map<String, TableColumnMetaData> tableColumnMetaDataMap,
                                Map<String, Object> properties, List<Map<String, Object>> batchList) {
            if(Objects.equals("insert",this.model) || Objects.equals("upsert",this.model)){
                executeSql(sql);
            } else if(Objects.equals("insertBatch",this.model) || Objects.equals("upsertBatch",this.model)){
                batchList.add(properties);
                if(this.batchSize == batchList.size()){
                    executeSql(sql);
                    batchList.clear();
                }
            }else if(Objects.equals("update",this.model)){
                executeSql(sql);
            }else if(Objects.equals("insertUpdate",this.model)){
                boolean flag = executeSql(sql);
                if(!flag){
                    String updateSql = ColumnDataUtil.transformUpdateSql(tableName,tableColumnOrderList,this.tableColumnMetaDataMap,this.updateWhereColumn,properties);
                    if(Objects.isNull(updateSql) || Objects.equals("",updateSql)){
                        return;
                    }
                    executeSql(updateSql);
                }
            }else{
                log.info("不支持的模式：{}",this.model);
            }

        }

        private boolean executeSql(String sql){
            PreparedStatement preparedStatement = null;
            Connection connection = null;
            try {
                connection = ImpalaUtil.defaultDataSource().getConnection();
                preparedStatement = connection.prepareStatement(sql);
                return preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                log.info("执行SQL失败! sql: {}",sql);
                return false;
            }finally {
                if(!Objects.isNull(preparedStatement)){
                    try {
                        preparedStatement.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                if(!Objects.isNull(connection)){
                    try {
                        connection.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }

        public void init() {
            this.readerConfig = super.getPluginJobConf();
            this.tableName = this.readerConfig.getString(KeyConstant.TABLE);
            this.model = this.readerConfig.getString(KeyConstant.MODEL);
            this.batchSize = this.readerConfig.getInt(KeyConstant.BATCH_SIZE,500);
            this.updateWhereColumn = this.readerConfig.getList(KeyConstant.UPDATE_WHERE_COLUMN, new ArrayList());
            JSONArray saColumnJsonArray = readerConfig.get(KeyConstant.SA_COLUMN, JSONArray.class);
            if(Objects.isNull(saColumnJsonArray)){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"column不应该为空！");
            }
            String saColumnStr = saColumnJsonArray.toJSONString();
            this.saColumnList = JSONObject.parseArray(saColumnStr, SaColumnItem.class);
            if(Objects.isNull(saColumnList) || saColumnList.isEmpty()){
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"column不应该为空！");
            }
            for (SaColumnItem col : saColumnList) {
                if(!(!Objects.isNull(col.getExclude()) && col.getExclude())){
                    this.tableColumnOrderList.add(col.getName());
                }
                List<DataConverter> dataConverters = col.getDataConverters();
                if(Objects.isNull(dataConverters) || dataConverters.isEmpty()){
                    continue;
                }
                dataConverters.forEach(con->{
                    con.setConverter(ConverterFactory.converterPrototype(con.getType()));
                });
            }

            String tableColumnMetaDataStr = readerConfig.get(KeyConstant.TABLE_COLUMN_META_DATA, String.class);
            if(Objects.isNull(tableColumnMetaDataStr) || Objects.equals("",tableColumnMetaDataStr)){
                String table = readerConfig.getString(KeyConstant.TABLE);
                throw new DataXException(CommonErrorCode.CONFIG_ERROR,"获取表["+table+"]列元数据时发生错误.");
            }

            List<TableColumnMetaData> tableColumnMetaDataList = JSONObject.parseArray(tableColumnMetaDataStr, TableColumnMetaData.class);

            this.tableColumnMetaDataMap = tableColumnMetaDataList.stream().collect(Collectors.toMap(TableColumnMetaData::getName, a -> a,(k1, k2)->k1));
            String SaPluginStr = readerConfig.getString(KeyConstant.PLUGIN,"[]");
            List<SaPlugin> SaPluginList = JSONObject.parseArray(SaPluginStr, SaPlugin.class);
            if(!Objects.isNull(SaPluginList) && !SaPluginList.isEmpty()){
                basePluginList = new ArrayList<>();
            }

            SaPluginList.forEach(saPlugin -> {
                String pluginName = saPlugin.getName();
                String pluginClass = saPlugin.getClassName();
                Map<String, Object> pluginParam = saPlugin.getParam();
                if(!NullUtil.isNullOrBlank(pluginName) && !NullUtil.isNullOrBlank(pluginClass)){
                    if(Objects.isNull(pluginParam)){
                        pluginParam = new HashMap<>();
                    }
                    basePluginList.add(PluginClassLoader.getBasePlugin(saPlugin.getName(), pluginClass, pluginParam));
                }

            });
        }

        public void destroy() {}
    }


}
