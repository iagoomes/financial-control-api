package br.com.iagoomes.financialcontrol.domain.mapper;

import br.com.iagoomes.financialcontrol.domain.entity.User;
import br.com.iagoomes.financialcontrol.infra.repository.entity.UserData;
import org.springframework.stereotype.Component;

/**
 * Mapper between User domain entity and UserData infrastructure entity
 */
@Component
public class UserMapper {

    /**
     * Maps UserData (infrastructure) to User (domain)
     */
    public User toDomain(UserData userData) {
        if (userData == null) {
            return null;
        }

        User user = new User();
        user.setId(userData.getId());
        user.setName(userData.getName());
        user.setEmail(userData.getEmail());
        user.setCreatedAt(userData.getCreatedAt());

        return user;
    }

    /**
     * Maps User (domain) to UserData (infrastructure)
     */
    public UserData toData(User user) {
        if (user == null) {
            return null;
        }

        return UserData.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}