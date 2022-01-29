package com.example;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;

/**
 * mqtt配置
 *
 * @author dragon
 * @date 2022/01/29
 */
@ApplicationScoped
public class MqttConfig {
    @Incoming("source-in")
    public void consumer(byte[] payload) {
        System.out.println(new String(payload));
    }
}
