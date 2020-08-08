package io.anyway.bigbang.framework.mqclient.domain;


public enum PurgeJobType {
    DEFAULT_PURGE_JOB("default"),
    CLIENT_MESSAGE_PURGE_JOB("client"),
    IDEMPOTENT_PURGE_JOB("idempotent"),
    ;

    private String code;

    PurgeJobType(String code) {
        this.code = code;
    }

    public static PurgeJobType getPurgeJobTypeByCode(String code) {
        for (PurgeJobType purgeJobType : PurgeJobType.values()) {
            if (purgeJobType.code.equals(code)) {
                return purgeJobType;
            }
        }
        return DEFAULT_PURGE_JOB;
    }
}
