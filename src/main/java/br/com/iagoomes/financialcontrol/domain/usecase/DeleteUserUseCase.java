package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Use case for deleting a user
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserProvider userProvider;

    public void execute(String userId) {
        log.debug("Executing DeleteUserUseCase for user ID: {}", userId);

        // Validate user exists
        if (!userProvider.existsById(userId)) {
            throw new BusinessException("User not found: " + userId);
        }

        // Delete user
        userProvider.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }
}