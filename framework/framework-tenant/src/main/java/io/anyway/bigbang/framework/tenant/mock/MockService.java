package io.anyway.bigbang.framework.tenant.mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MockService {

    void invoke(HttpServletRequest request, HttpServletResponse response);
}
