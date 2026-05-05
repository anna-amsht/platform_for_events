package com.unik.userservice.service;

import com.unik.userservice.dto.request.UpdateMyProfileRequest;
import com.unik.userservice.dto.request.UpsertUserProfileRequest;
import com.unik.userservice.repository.InterestRepository;
import com.unik.userservice.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserProfileServiceIntegrationTest {
    @Autowired
    private UserProfileService service;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Test
    void upsertUpdateAndSearchRoundTripThroughJpa() {
        UUID userId = UUID.randomUUID();
        service.upsert(new UpsertUserProfileRequest(userId, " User@Example.com "));
        service.updateMyProfile(userId, new UpdateMyProfileRequest(
                "Ada",
                "Lovelace",
                "Analytical Engines",
                "Engineer",
                "Minsk",
                null,
                "Bio",
                List.of("Java", "Spring Boot")
        ));

        var profile = service.getProfile(userId);
        var search = service.search("ada", "spring boot", 0, 10);

        assertThat(profile.email()).isEqualTo("user@example.com");
        assertThat(profile.interests()).extracting("slug").containsExactly("java", "spring-boot");
        assertThat(search.content()).extracting("id").containsExactly(userId);
        assertThat(userProfileRepository.findById(userId)).isPresent();
        assertThat(interestRepository.findAll()).hasSize(2);
    }
}
