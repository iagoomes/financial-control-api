package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Use case for updating user information
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserProvider userProvider;

    public User execute(String userId, String name, String email) {
        log.debug("Executing UpdateUserUseCase for user ID: {}", userId);

        // Find user
        User user = userProvider.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId));

        // If email is being changed, validate it's not already taken
        if (email != null && !email.equals(user.getEmail())) {
            if (userProvider.existsByEmail(email)) {
                throw new BusinessException("Email already registered: " + email);
            }
        }

        // Update user information
        user.updateInfo(name, email);

        // Save and return
        User updatedUser = userProvider.save(user);
        log.info("User updated successfully: {}", userId);

        return updatedUser;
    }
}