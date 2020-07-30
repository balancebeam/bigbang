package io.anyway.bigbang.example.base.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import io.anyway.bigbang.example.base.domain.User;
import org.apache.http.client.fluent.Request;

public class UserFetcher {

  private final String url;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public UserFetcher(String url) {
    this.url = url;
  }

  public User fetch(long userId) {
    try {
      String body = Request.Get(url + "/users/" + userId)
          .execute()
          .returnContent()
          .asString();

      return objectMapper.readValue(body, User.class);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
