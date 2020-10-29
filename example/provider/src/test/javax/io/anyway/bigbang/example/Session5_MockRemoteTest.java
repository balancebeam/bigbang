package io.anyway.bigbang.example;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.anyway.bigbang.example.base.domain.User;
import io.anyway.bigbang.example.base.services.UserFetcher;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class Session5_MockRemoteTest {

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  @BeforeClass
  public static void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    User user= new User(1,"jacky");
    String body;
    try {
      body = objectMapper.writeValueAsString(user);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
    stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse().withBody(body)));
  }

  @Test
  public void shouldAdaptJsonToUser() {
    UserFetcher fetcher = new UserFetcher("http://localhost:" + wireMockRule.port());

    User user = fetcher.fetch(1L);

    assertThat(user.id()).isOne();
    assertThat(user.name()).isEqualTo("jacky");
  }
}
