package io.anyway.bigbang.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import io.anyway.bigbang.example.base.domain.User;
import io.anyway.bigbang.example.base.domain.UserRepository;
import io.anyway.bigbang.example.base.services.UserService;
import org.junit.Test;
import org.mockito.Mockito;

public class Session4_MockTest {

  private final UserRepository userRepository = Mockito.mock(UserRepository.class);
  private final User user = new User("jack");

  @Test
  public void shouldFindUser() {
    Mockito.when(userRepository.findOne(0L)).thenReturn(Optional.of(user));

    UserService userService = new UserService(userRepository);

    Optional<User> user = userService.findUser(0L);

    assertThat(user).isPresent();
    assertThat(user).contains(this.user);
  }

  @Test
  public void shouldSaveUser() {
    Mockito.doAnswer(invocation-> {
      User user= invocation.getArgument(0);
      user.setId(1L);
      return null;
    }).when(userRepository).save(user);

    UserService userService = new UserService(userRepository);

    long userId = userService.save(user);

    assertThat(userId).isOne();
  }

  @Test
  public void shouldSaveUserOnceOnly() {
    //mock userRepository.save(User)
    Mockito.doAnswer(invocation-> {
      User user= invocation.getArgument(0);
      user.setId(1L);
      return null;
    }).when(userRepository).save(user);
    //mock userRepository.findOne(name)
    Mockito.doAnswer(invocation->
      user.id()!= 0? Optional.of(user): Optional.empty()
    ).when(userRepository).findOne(user.name());

    UserService userService = new UserService(userRepository);

    userService.save(user);
    long userId = userService.save(user);

    assertThat(userId).isOne();

    Mockito.verify(userRepository).save(user);
  }

  @Test (expected = IllegalStateException.class)
  public void shouldThrowIfDatabaseIsDown() {
    Mockito.when(userRepository.findOne(0L))
            .thenThrow(new IllegalStateException("IllegalStateException"));

    UserService userService = new UserService(userRepository);

    userService.findUser(0L);
  }
}
