package com.example;


import io.agroal.api.AgroalDataSource;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.influxdb.dto.QueryResult;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Path("/")
@Slf4j
public class MyController {
    private final Random random = new Random();

    @Inject
    InfluxDbConfig influxDBConfig;
    @Inject
    AgroalDataSource sqliteDb;

    @Channel("source-out")
    Emitter<String> quoteRequestEmitter;

    @GET
    @Path("/save")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Data> saveData() throws SQLException {
        //随机温度
        int temperature = random.nextInt(21) + 16;
        Data data = new Data();
        data.setSensorName("testSensor");
        data.setTemperature(temperature);
        HashMap<String, String> tagMap = new HashMap<>();
        tagMap.put("id", "1");
        HashMap<String, Object> filedMap = new HashMap<>();
        filedMap.put("temperature", temperature);
        return Uni.createFrom().completionStage(CompletableFuture.supplyAsync(
                        () -> {
                            //保存至Influx
                            influxDBConfig.insert("test", "temperature", tagMap, filedMap);
                            //发送至MQ
                            quoteRequestEmitter.send(String.valueOf(temperature));
                            try {
                                sqliteDb.getConnection().nativeSQL("update temperature_data set temperature=" + temperature + " where id =1");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            return data;
                        }
                )
        );
    }

    @GET
    @Path("/queryResult")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<QueryResult> getQueryResult() {
        return Uni.createFrom().completionStage(CompletableFuture.supplyAsync(() -> influxDBConfig.query("test", "SELECT MEAN(temperature) FROM \"temperature\" WHERE time > now() - 20m")));
    }

    @GET
    @Path("/testBlock")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> testBlock() {

        log.info("testBlock in");
        Uni<String> stringUni = Uni.createFrom().completionStage(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000L);
                log.info("testBlock result");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "ok";

        }));
        log.info("testBlock out");
        return stringUni;
    }

    /**
     * 数据实体
     */
    private static class Data {
        /**
         * 温度
         */
        private int temperature;
        /**
         * 传感器名
         */
        private String sensorName;

        public int getTemperature() {
            return temperature;
        }

        public void setTemperature(int temperature) {
            this.temperature = temperature;
        }

        public String getSensorName() {
            return sensorName;
        }

        public void setSensorName(String sensorName) {
            this.sensorName = sensorName;
        }
    }
}
