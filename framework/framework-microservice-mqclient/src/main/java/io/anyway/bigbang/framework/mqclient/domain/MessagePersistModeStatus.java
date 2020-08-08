package io.anyway.bigbang.framework.mqclient.domain;

public enum MessagePersistModeStatus {
    KEEP_AFTER_SENT(1),
    BURN_AFTER_SENT(2),
    BURN_BEFORE_SEND(0);
    private int val;

    MessagePersistModeStatus(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static MessagePersistModeStatus of(int val) {
        switch (val) {
            case 1:
                return KEEP_AFTER_SENT;
            case 2:
                return BURN_AFTER_SENT;
            default:
                return BURN_BEFORE_SEND;
        }
    }
}
