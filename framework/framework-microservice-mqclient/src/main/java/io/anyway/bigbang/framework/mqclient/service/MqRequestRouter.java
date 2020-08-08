package io.anyway.bigbang.framework.mqclient.service;


import io.anyway.bigbang.framework.mqclient.domain.MqClientMessage;

public interface MqRequestRouter {

    boolean route(MqClientMessage mqClientMessage);
}
