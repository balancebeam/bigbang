package io.anyway.bigbang.framework.session;


public interface UserDetailContext {

    String USER_HEADER_NAME="x-user-detail";

    default String getAppId() {
        return "";
    }

    String getUserId();

    String getUserName();

    default String getType(){
        return "";
    }

}
