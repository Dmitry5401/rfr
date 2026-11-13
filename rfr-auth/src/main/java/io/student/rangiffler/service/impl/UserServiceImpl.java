package io.student.rangiffler.service.impl;

import io.student.rangiffler.data.UserEntity;
import io.student.rangiffler.data.repository.UserRepository;
import io.student.rangiffler.service.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserServiceImpl implements UserService {

  private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public String registerUser(String username, String password) {
    UserEntity userEntity = UserEntity.register(username, passwordEncoder.encode(password));
    return userRepository.save(userEntity).getUsername();
  }
}
