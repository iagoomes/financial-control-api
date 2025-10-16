package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.app.mapper.AppMapper;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.domain.usecase.CreateUserUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.DeleteUserUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.GetUserByIdUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.ListUsersUseCase;
import br.com.iagoomes.financialcontrol.domain.usecase.UpdateUserUseCase;
import br.com.iagoomes.financialcontrol.model.CreateUserRequest;
import br.com.iagoomes.financialcontrol.model.UpdateUserRequest;
import br.com.iagoomes.financialcontrol.model.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Application service for User operations
 * Orchestrates use cases and converts between DTOs and domain entities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final AppMapper appMapper;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Service: Creating user with email: {}", request.getEmail());

        User user = createUserUseCase.execute(request.getName(), request.getEmail());

        return appMapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Service: Updating user: {}", userId);

        User user = updateUserUseCase.execute(
                userId.toString(),
                request.getName(),
                request.getEmail()
        );

        return appMapper.toUserDTO(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Service: Deleting user: {}", userId);
        deleteUserUseCase.execute(userId.toString());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID userId) {
        log.info("Service: Getting user by ID: {}", userId);

        return getUserByIdUseCase.execute(userId.toString())
                .map(appMapper::toUserDTO);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> listUsers(Integer page, Integer size) {
        log.info("Service: Listing users - page: {}, size: {}", page, size);

        return listUsersUseCase.execute(page, size)
                .map(appMapper::toUserDTO);
    }
}