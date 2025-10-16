package br.com.iagoomes.financialcontrol.infra.dataprovider;

import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.domain.mapper.UserMapper;
import br.com.iagoomes.financialcontrol.infra.repository.UserDataRepository;
import br.com.iagoomes.financialcontrol.infra.repository.entity.UserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of UserProvider using JPA repository
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataProvider implements UserProvider {

    private final UserDataRepository userDataRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        UserData userData = userMapper.toData(user);
        UserData savedData = userDataRepository.save(userData);
        return userMapper.toDomain(savedData);
    }

    @Override
    public Optional<User> findById(String id) {
        log.debug("Finding user by ID: {}", id);
        return userDataRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userDataRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsById(String id) {
        log.debug("Checking if user exists by ID: {}", id);
        return userDataRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userDataRepository.findByEmail(email).isPresent();
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting user by ID: {}", id);
        userDataRepository.deleteById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        log.debug("Finding all users with pagination: {}", pageable);
        return userDataRepository.findAll(pageable)
                .map(userMapper::toDomain);
    }
}