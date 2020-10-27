package io.anyway.bigbang.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.anyway.bigbang.example.controller.ProviderExternalController;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.example.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@RunWith(SpringRunner.class)
@WebMvcTest(ProviderExternalController.class)
public class Session6_ControllerTest {
  private final User jack = new User("jack","M",24);

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService personService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void shouldReturnExpectedPerson() throws Exception {

    when(personService.getUser("jack")).thenReturn(Optional.of(jack));

    String body= mockMvc.perform(get("/api/person/{name}", "jack"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    assertThat(body).isEqualTo(objectMapper.writeValueAsString(jack));
  }

  @Test
  public void shouldReturn500() throws Exception {

    when(personService.getUser("tom")).thenReturn(null);
    mockMvc.perform(get("/api/person/{name}", "tom"))
            .andExpect(status().is(500));
  }
}
