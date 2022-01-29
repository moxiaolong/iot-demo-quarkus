package com.example;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * db config influx
 *
 * @author dragon
 * @date 2022/01/29
 */
@ApplicationScoped
public class InfluxDbConfig {

    private InfluxDB influxDB;
    @ConfigProperty(name = "influx.url")
    String url;
    @ConfigProperty(name = "influx.user")
    String user;
    @ConfigProperty(name = "influx.password")
    String password;

    @PostConstruct
    public void init() {
        this.influxDB = InfluxDBFactory.connect(url, user, password);
    }


    /**
     * 插入
     *
     * @param measurement 表
     * @param tags        标签
     * @param fields      字段
     * @param database    数据库
     */
    public void insert(String database, String measurement, Map<String, String> tags, Map<String, Object> fields) {
        Point.Builder builder = Point.measurement(measurement);
        builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        builder.tag(tags);
        builder.fields(fields);
        influxDB.write(database, "", builder.build());
    }

    /**
     * 查询
     *
     * @param command 查询语句
     */
    public QueryResult query(String database, String command) {
        return influxDB.query(new Query(command, database));
    }


}