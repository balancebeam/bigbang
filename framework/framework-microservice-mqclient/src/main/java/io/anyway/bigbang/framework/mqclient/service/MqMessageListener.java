package io.anyway.bigbang.framework.mqclient.service;


import io.anyway.bigbang.framework.mqclient.domain.MqClientMessage;


public interface MqMessageListener {

    void onMessage(MqClientMessage mqClientMessage);
}
