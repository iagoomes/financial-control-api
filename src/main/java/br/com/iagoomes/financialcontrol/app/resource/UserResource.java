package br.com.iagoomes.financialcontrol.app.resource;

import br.com.iagoomes.financialcontrol.api.UsersApiDelegate;
import br.com.iagoomes.financialcontrol.app.service.UserService;
import br.com.iagoomes.financialcontrol.infra.exception.BusinessException;
import br.com.iagoomes.financialcontrol.model.CreateUserRequest;
import br.com.iagoomes.financialcontrol.model.UpdateUserRequest;
import br.com.iagoomes.financialcontrol.model.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Resource implementation for Users API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserResource implements UsersApiDelegate {

    private final UserService userService;

    @Override
    public CompletableFuture<ResponseEntity<UserDTO>> createUser(CreateUserRequest createUserRequest) {
        try {
            log.info("Resource: Creating user with email: {}", createUserRequest.getEmail());

            UserDTO userDTO = userService.createUser(createUserRequest);

            // Build location header
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/users/{id}")
                    .buildAndExpand(userDTO.getId())
                    .toUri();

            return CompletableFuture.completedFuture(
                    ResponseEntity.created(location).body(userDTO)
            );

        } catch (BusinessException e) {
            log.warn("Resource: Business error creating user: {}", e.getMessage());
            if (e.getMessage().contains("already registered")) {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.CONFLICT).build());
            }
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());

        } catch (Exception e) {
            log.error("Resource: Error creating user", e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<UserDTO>> getUser(UUID userId) {
        try {
            log.info("Resource: Getting user by ID: {}", userId);

            Optional<UserDTO> userDTO = userService.getUserById(userId);

            return userDTO
                    .map(dto -> CompletableFuture.completedFuture(ResponseEntity.ok(dto)))
                    .orElseGet(() -> CompletableFuture.completedFuture(ResponseEntity.notFound().build()));

        } catch (Exception e) {
            log.error("Resource: Error getting user by ID: {}", userId, e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<List<UserDTO>>> listUsers(Integer page, Integer size) {
        try {
            log.info("Resource: Listing users - page: {}, size: {}", page, size);

            Page<UserDTO> usersPage = userService.listUsers(page, size);

            return CompletableFuture.completedFuture(
                    ResponseEntity.ok(usersPage.getContent())
            );

        } catch (Exception e) {
            log.error("Resource: Error listing users", e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<UserDTO>> updateUser(UUID userId, UpdateUserRequest updateUserRequest) {
        try {
            log.info("Resource: Updating user: {}", userId);

            UserDTO userDTO = userService.updateUser(userId, updateUserRequest);

            return CompletableFuture.completedFuture(ResponseEntity.ok(userDTO));

        } catch (BusinessException e) {
            log.warn("Resource: Business error updating user: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
            }
            if (e.getMessage().contains("already registered")) {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.CONFLICT).build());
            }
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());

        } catch (Exception e) {
            log.error("Resource: Error updating user: {}", userId, e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> deleteUser(UUID userId) {
        try {
            log.info("Resource: Deleting user: {}", userId);

            userService.deleteUser(userId);

            return CompletableFuture.completedFuture(ResponseEntity.noContent().build());

        } catch (BusinessException e) {
            log.warn("Resource: Business error deleting user: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
            }
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());

        } catch (Exception e) {
            log.error("Resource: Error deleting user: {}", userId, e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().build());
        }
    }
}