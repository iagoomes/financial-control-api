package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Use case for retrieving a user by ID
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetUserByIdUseCase {

    private final UserProvider userProvider;

    public Optional<User> execute(String userId) {
        log.debug("Executing GetUserByIdUseCase for user ID: {}", userId);
        return userProvider.findById(userId);
    }
}