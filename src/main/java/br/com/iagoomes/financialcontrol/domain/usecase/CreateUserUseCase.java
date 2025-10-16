package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Use case for creating a new user
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserProvider userProvider;

    public User execute(String name, String email) {
        log.debug("Executing CreateUserUseCase for email: {}", email);

        // Validate that email is not already registered
        if (userProvider.existsByEmail(email)) {
            throw new BusinessException("Email already registered: " + email);
        }

        // Create new user
        User user = User.create(name, email);

        // Save and return
        User savedUser = userProvider.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return savedUser;
    }
}