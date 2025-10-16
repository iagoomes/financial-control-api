package br.com.iagoomes.financialcontrol.domain.usecase;

import br.com.iagoomes.financialcontrol.domain.UserProvider;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Use case for listing users with pagination
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserProvider userProvider;

    public Page<User> execute(Integer page, Integer size) {
        log.debug("Executing ListUsersUseCase - page: {}, size: {}", page, size);

        // Set defaults if not provided
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        // Validate pagination parameters
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize < 1 || pageSize > 200) {
            pageSize = 20;
        }

        // Create pageable with sorting by creation date (newest first)
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        return userProvider.findAll(pageable);
    }
}