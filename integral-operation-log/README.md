## 开启切面
```
@SpringBootApplication
@EnableGlobalMethodOperationLog
public class AutosdkDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutosdkDemoApplication.class, args);
    }

}

```
## 定义获取元数据的handler
可在这里做日志持久化操作
 ```
@Component
public class SDKOperationLogProcessHandle implements OperationLogProcessHandle {
    @Override
    public void process(List<OperationLogMeta> list) {
       System.out.println("操作日志元数据process"+ JSON.toJSONString(list));
    }
}

  ```  

## 增加单个操作日志埋点
```
curl --location 'http://localhost:8080/createSdk' \
--header 'Content-Type: application/json' \
--data '{
  "projectName": "yxt-order-open-sdk",
  "groupId": "com.yxt",
  "artifactId": "yxt-order-open-sdk",
  "version": "1.0.0.SNAPSHOT",
  "packageName": "com.yxt.order"
}
'
```

 ```
   @RequestMapping("/createSdk")
    @ResponseBody
    @OperationLog(operationType ="2",
            description = "创建了SDK模版名字为【#{params[projectName]}】模板",
            targetType ="ADD",
            targetId = "#{id}",
            beforeValue = "#{params}",
            ip = "#{ip}",
            userId = "#{userId}")
    public String createSDK(@RequestBody Map<String, String> params) {
        OperationLogContext.add("ip","192.168.10.1");
        OperationLogContext.add("id",111);
        OperationLogContext.add("userId",222);
        // 返回响应
        return "Hello ";
    }
 ```
## 批量操作日志埋点 
batchTarget属性
```
curl --location 'http://localhost:8080/batchCreateSdk' \
--header 'Content-Type: application/json' \
--data '[
    {
        "projectName": "yxt-order-open-sdk",
        "groupId": "com.yxt",
        "artifactId": "yxt-order-open-sdk",
        "version": "1.0.0.SNAPSHOT",
        "packageName": "com.yxt.order"
    },
    {
        "projectName": "yxt-order-open-sdk2",
        "groupId": "com.yxt2",
        "artifactId": "yxt-order-open-sdk2",
        "version": "1.0.0.SNAPSHOT2",
        "packageName": "com.yxt.order2"
    }
]'

   @RequestMapping("/batchCreateSdk")
    @ResponseBody
    @OperationLog(operationType ="2",
            description = "创建了SDK模版名字为【#{item[projectName]}】模板",
            targetType ="ADD",
            batchTarget = "#{params}",
            targetId = "#{item[id]}",
            beforeValue = "#{item}",
            ip = "#{ip}",
            userId = "#{userId}")
    public String createSDK(@RequestBody List<Map<String, String>> params) {
        OperationLogContext.add("ip","192.168.10.1");
        int i=0;
        for (Map<String, String> param : params) {
            param.put("id",String.valueOf(i++));
        }

        OperationLogContext.add("userId",222);
        // 返回响应
        return "Hello ";
    }

```
## 前置条件判断notesExpression
比如新增或者修改失败
```
  @RequestMapping("/createSdk")
    @ResponseBody
    @OperationLog(operationType ="2",
            notesExpression = "#{id>0}",//id大于0表示新增成功 修改同理 判断受影响函数
            description = "创建了SDK模版名字为【#{params[projectName]}】模板",
            targetType ="ADD",
            targetId = "#{id}",
            beforeValue = "#{params}",
            ip = "#{ip}",
            userId = "#{userId}")
    public String createSDK(@RequestBody Map<String, String> params) {
        OperationLogContext.add("ip","192.168.10.1");
        OperationLogContext.add("id",111);
        OperationLogContext.add("userId",222);
        // 返回响应
        return "Hello ";
    }
```

## 关于el语法
map采用 #{item[projectName]}
对象采用 #{item.projectName}