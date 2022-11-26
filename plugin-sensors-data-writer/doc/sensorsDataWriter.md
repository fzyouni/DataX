## 快速介绍

```sensorsDataWriter```是用户将数据导入到神策分析的插件。

## **实现原理**

```sensorsDataWriter```是通过神策分析java SDK将数据生成符合神策分析的数据格式，也支持通过神策分析数据接口接收数据（数据不落磁盘）。

## 配置说明

```json
{
  "job": {
    "content": [
      {
        "reader": {
          "name": "xxxx",
          "parameter": {
            ...
          }
        },
        "writer": {
          "name": "sensorsDataWriter",
          "parameter": {
            "type": "item",
            "sdkLogPathAddress": "/datax/datax/logag/log",
            "sdkNetAddress": "",
            "columns": [
              {
                "index": 0,
                "name": "name1",
                "ifNullGiveUp": true
              },
              {
                "index": 1,
                "name": "age1",
                "dataConverters": [
                  {
                    "type": "Number2Str"
                  }
                ]
              },
              {
                "index": 2,
                "name": "id2",
                "dataConverters": [
                  {
                    "type": "BigInt2Date"
                  },
                  {
                    "type": "Date2Str",
                    "param": {
                      "pattern": "yyyy-MM"
                    }
                  }
                ]
              }
            ],
            "track": {
              "idm3": true,
              "identities": [
                {
                  "sensorsIdKeyName": "identity_test",
                  "sensorIdValueColumn": "id1"
                }
              ],
              "eventName": "testEventName",
              "distinctIdName": "test123",
              "isLoginId": true
            },
            "profileSet": {
              "idm3": true,
              "identities": [
                {
                  "sensorsIdKeyName": "identity_test",
                  "sensorIdValueColumn": "id1"
                }
              ]
            },
            "itemSet": {
              "itemIdColumn": "name1",
              "itemType": "test"
            }
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": "1"
      }
    }
  }
}
```

## **参数说明**

​        `sdkLogPathAddress`：本地存放日志路径，确保日志文件夹路径存在，并且程序有写权限。 ​        `sdkNetAddress`：神策服务器数据接受地址，确保数据接受地址正常可访问。
以上两个参数需至少传入一个；

​        `type`：导入神策分析的数据类型，可取值有：track/user/item，分别对应神策的事件/用户/属性。必传属性

​        `track`：`type`为 event 时，目前支持事件类型 track。 ​        `profileSet`：`type`为 user 时，目前支持事件类型 profileSet。
​        `itemSet`：`type`为 item 时，目前支持事件类型 itemSet。 根据 `type` 类型确定事件类型节点

​        `columns`：与读插件的 column 对应映射，若 track/profileSet/itemSet 内部有节点映射到 column,则不会出现在 properties。

​        `column.index`：该列使用读插件字段的下标索引，从0开始。必传属性 

​        `column.name`：column 转化之后的列名称，入库名称。必传属性

​        ```column.ifNullGiveUp```：当该列值经过转换器转换后为空时，是否丢弃该行数据，默认值为false。 

​        `column.dataConverters`
：将dataX读出的数据转换为其他类型或者值，所使用的数据转换器。插件支持的数据转换器见下文``内置数据转换器``，支持多个联合使用。 ​        ``column.dataConverters.type``
：使用内置转换器的名称，见下文``内置数据转换器`` ``转换器type``列。

​        `column.dataConverters.param`
：使用内置转换器时，转换器必要的参数列表，参数key根据不同转换器不同而不同，见下文``内置数据转换器参数说明``。

track 事件是否为 IDM3.0

​        `track.idm3`：track 事件是否为神策 IDM3.0 数据，默认值 false。

track 事件名参数，支持两种方式获取，1：指定固定值为导入事件名；2：根据读插件里面的 column 来映射成事件名称；必传参数。

​        `track.eventName`：指定固定值为导入事件名。 ​        `track.eventNameColumn`：对应 writer 插件里面的 columns 中的其中一个 column 的
name。注意：当选定一个 column 作为事件名，就不会出现在自定义属性集合中；

track 事件 distinctId 参数,该参数在开启 IDM3.0 时，非必传参数，默认值为 identities 集合中第一个纬度标识；在未开启 IDM3.0 时，该参数是必传参数；
该参数支持两种方式获取，1：指定固定值为导入事件名；2：根据读插件里面的 column 来映射成事件名称；必传参数

​        `track.distinctIdName`：指定固定值为导入事件名。 ​        `track.distinctIdColumn`：指定 columns 中的 column 作为 distinctId。

track 事件 isLoginId 参数，该参数在开启 IDM3.0 之后，是无用参数，无需额外设置； 未开启 IDM3.0 时，表示 distinctId 是否为登录 ID ；默认值为 false

​        `track.isLoginId`：表示 distinctId 是否为登录 ID ；默认值为 false。

track 事件 identities 参数，该参数为 IDM3.0 参数；即开启 IDM3.0 时，必须要设置； 未开启 IDM3.0 时，传值不生效；

​        `track.identities`：IDM3.0 用户多维度标识集合。

identities 节点信息，本质上是一个 K-V 存储结构；K 标识用户纬度信息，V 标识用户纬度值； key 获取方式有两种；1：指定固定值，2：指定固定列对应的值；

​        `identity.sensorsIdKeyName`：指定固定值，后续生成事件中 identity 中 key 均为该值； ​        `identity.sensorIdKeyColumn`：指定列，生成事件中的
key 都为列对应的值；

value 获取方式有两种；1：指定固定值，2：指定固定列对应的值；

​        `identity.sensorIdValueName`：指定固定值，后续生成事件 identity 中 value 均为该值； ​        `track.sensorIdValueColumn`
：指定列，生成事件中的 value 都为列对应的值；

profileSet 事件节点

​        `profileSet.idm3`：是否为 IDM3.0 数据。

​        `profileSet.identities`：节点信息与 `track.identities` 完全一致，请参考 `track.identities`。

​        `profileSet.distinctIdColumn`：与 `track.distinctIdColumn` 一致，请参考 `track.distinctIdColumn`。
​        `profileSet.distinctIdName`：与 `track.distinctIdName` 一致，请参考 `track.distinctIdName`。

​        `profileSet.isLoginId`：与 `track.isLoginId` 一致，请参考 `track.isLoginId`。

itemSet 事件节点

itemId 节点目前只支持从 column 里获取； 

​        `itemSet.itemIdColumn`：type为item时，作为神策itemId的列，该属性应该在column列表中，并且该属性不能存在空值。

itemType 节点目前支持两种方式获取：1：指定固定值，2：指定列； 

​        `itemSet.itemType`：指定固定值 ​        `itemSet.itemTypeColumn`
：指定列，确保 `itemSet.itemTypeColumn`配置项是否在`columns`配置项的列表中。

## 内置数据转换器

|   转换器type   |        转换器全称        |                             功能                             |
| :------------: | :----------------------: | :----------------------------------------------------------: |
|    Date2Str    |    Date2StrConverter     |            将java.util.Date转换为java.long.String            |
|   Date2Long    |    Date2LongConverter    |                  将java.util.Date转换为long                  |
|    Long2Date   |     Long2DateConverter   |                   将时间戳转成 日期；支持pattern               |
|    IfNull      |     IfNullConverter      |                   将 null 值转成指定值，若不指定，默认返回 "NULL"      |

## 内置数据转换器参数说明

### Long2Date

​ 无参数要求

#### 示例

```json
"dataConverters":[
    {
      "type": "Long2Date"
    }
]
```

### Date2Long

无参数要求

#### 示例

```json
"dataConverters":[
    {
      "type": "Date2Long"
    }
]
```


### IfNull

```default```：遇到 null 值之后，指定转化的值


#### 示例

```json
"dataConverters":[
    {
        "type": "IfNull",
        "param": {
        "default":"NULL"
        }
    }
]
```
