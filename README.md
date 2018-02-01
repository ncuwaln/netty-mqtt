### build
* 启动部署环境<br/>

  在mqtt目录下运行
```
docker-compose up
```
* build<br/>
```
mvn clean package
```
### run
* 启动server
```
bin/start-server.sh
```
* 启动client
```
bin/start-client.sh
```

### 缺点
1. 未实现qos2
2. 未实现connect时的username与password
3. 只支持确定的topic, 不支持topic filter匹配
