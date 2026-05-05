package com.unik.userservice.service;

import com.unik.userservice.dto.request.UpdateMyProfileRequest;
import com.unik.userservice.dto.request.UpsertUserProfileRequest;
import com.unik.userservice.entity.Interest;
import com.unik.userservice.entity.UserProfile;
import com.unik.userservice.exception.UserNotFoundException;
import com.unik.userservice.repository.InterestRepository;
import com.unik.userservice.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {
    private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
    private final InterestRepository interestRepository = mock(InterestRepository.class);
    private final UserProfileService service = new UserProfileService(userProfileRepository, interestRepository);

    @Test
    void upsertCreatesProfileWithNormalizedEmail() {
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.upsert(new UpsertUserProfileRequest(userId, " User@Example.COM "));

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void updateProfileDeduplicatesAndCreatesInterestsBySlug() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.create(userId, "user@example.com");
        Interest java = Interest.create("Java", "java");
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(interestRepository.findAllBySlugIn(anyCollection())).thenReturn(List.of(java));
        when(interestRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateMyProfile(userId, new UpdateMyProfileRequest(
                " Ada ", " Lovelace ", " Company ", " Engineer ", " Minsk ", null, " Bio ",
                List.of("Java", " java ", "Spring Boot")
        ));

        assertThat(response.firstName()).isEqualTo("Ada");
        assertThat(response.lastName()).isEqualTo("Lovelace");
        assertThat(response.interests()).extracting("slug").containsExactly("java", "spring-boot");
        verify(interestRepository).saveAll(any());
    }

    @Test
    void getProfileThrowsWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
