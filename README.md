### Sentinel

```bash
   java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```

### Zipkin

```bash
   java -jar zipkin-server-2.23.9-exec.jar --zipkin.collector.rabbitmq.addresses=localhost:5672
```

#### Zipkin 使用Mysql数据库

1. https://github.com/openzipkin/zipkin/blob/master/zipkin-storage/mysql-v1/src/main/resources/mysql.sql
这是官方仓库中给的 MySQL 改造的脚本，创建一个 zipkin 的 db 库；
2. 从 https://github.com/openzipkin/zipkin/blob/16857b6cc3/zipkin-server/src/main/resources/zipkin-server-shared.yml 配置文件中看到 mysql 的配置项.
3. 启动命令添加上 MySQL 的即可。 我的命令：

```bash
java -jar zipkin-server-2.23.9-exec.jar --STORAGE_TYPE=mysql --MYSQL_HOST=127.0.0.1 --MYSQL_TCP_PORT=3306 --MYSQL_USER=root --MYSQL_PASS=123456 --MYSQL_DB=zipkin --RABBIT_ADDRESSES=127.0.0.1:5672
```

### 创建 ELK 容器

```bash
sudo docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk sebp/elk:7.16.1
```

```bash
docker exec -it elk /bin/bash
```


#### Logstash 配置
```bash
vi /etc/logstash/conf.d/02-beats-input.conf

```

```conf

input {
    beats {
        port => 5044
        codec => json_lines
    }
}

output {
    elasticsearch {
        hosts => ["localhost:9200"]
        index => "zerotone"
    }
}
```

```bash
docker restart elk
```

### filebeat

```bash
./filebeat-7.16.1/filebeat -e -c filebeat.yml
```


### Seata

```bash
./bin/seata-server.sh
```