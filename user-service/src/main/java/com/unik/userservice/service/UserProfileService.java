package com.unik.userservice.service;

import com.unik.userservice.dto.request.UpdateMyProfileRequest;
import com.unik.userservice.dto.request.UpsertUserProfileRequest;
import com.unik.userservice.dto.response.InterestResponse;
import com.unik.userservice.dto.response.PagedResponse;
import com.unik.userservice.dto.response.UserProfileResponse;
import com.unik.userservice.dto.response.UserSummaryResponse;
import com.unik.userservice.entity.Interest;
import com.unik.userservice.entity.UserProfile;
import com.unik.userservice.exception.UserNotFoundException;
import com.unik.userservice.repository.InterestRepository;
import com.unik.userservice.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserProfileService {

    private static final Pattern NON_LATIN_OR_DIGIT = Pattern.compile("[^a-z0-9]+");

    private final UserProfileRepository userProfileRepository;
    private final InterestRepository interestRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, InterestRepository interestRepository) {
        this.userProfileRepository = userProfileRepository;
        this.interestRepository = interestRepository;
    }

    @Transactional
    public UserProfileResponse upsert(UpsertUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(request.userId())
                .map(existing -> {
                    existing.syncEmail(normalizeEmail(request.email()));
                    return existing;
                })
                .orElseGet(() -> UserProfile.create(request.userId(), normalizeEmail(request.email())));
        return toProfileResponse(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UUID userId) {
        return toProfileResponse(loadProfile(userId));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        UserProfile profile = loadProfile(userId);
        profile.update(request, resolveInterests(request.interests()));
        return toProfileResponse(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return toProfileResponse(loadProfile(userId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> search(String query, String interest, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending().and(Sort.by("lastName").ascending()));
        Page<UserProfile> profiles = userProfileRepository.search(normalizeSearch(query), normalizeSlug(interest), pageable);
        List<UserSummaryResponse> content = profiles.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();
        return new PagedResponse<>(
                content,
                profiles.getNumber(),
                profiles.getSize(),
                profiles.getTotalElements(),
                profiles.getTotalPages(),
                profiles.isFirst(),
                profiles.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<InterestResponse> getAllInterests() {
        return interestRepository.findAll(Sort.by("name").ascending()).stream()
                .map(this::toInterestResponse)
                .toList();
    }

    private UserProfile loadProfile(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Set<Interest> resolveInterests(List<String> rawInterests) {
        if (rawInterests == null || rawInterests.isEmpty()) {
            return Set.of();
        }

        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        for (String rawInterest : rawInterests) {
            String name = normalizeInterestName(rawInterest);
            if (name == null) {
                continue;
            }
            normalized.putIfAbsent(toSlug(name), name);
        }
        if (normalized.isEmpty()) {
            return Set.of();
        }

        LinkedHashMap<String, Interest> interestsBySlug = new LinkedHashMap<>();
        for (Interest interest : interestRepository.findAllBySlugIn(normalized.keySet())) {
            interestsBySlug.put(interest.getSlug(), interest);
        }

        List<Interest> created = new ArrayList<>();
        for (var entry : normalized.entrySet()) {
            if (!interestsBySlug.containsKey(entry.getKey())) {
                created.add(Interest.create(entry.getValue(), entry.getKey()));
            }
        }
        for (Interest interest : interestRepository.saveAll(created)) {
            interestsBySlug.put(interest.getSlug(), interest);
        }

        Set<Interest> result = new LinkedHashSet<>();
        for (String slug : normalized.keySet()) {
            result.add(interestsBySlug.get(slug));
        }
        return result;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSearch(String query) {
        return StringUtils.hasText(query) ? query.trim() : null;
    }

    private String normalizeSlug(String value) {
        return StringUtils.hasText(value) ? toSlug(value.trim()) : null;
    }

    private String normalizeInterestName(String rawInterest) {
        if (!StringUtils.hasText(rawInterest)) {
            return null;
        }
        String trimmed = rawInterest.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toSlug(String rawValue) {
        String normalized = Normalizer.normalize(rawValue, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        String slug = NON_LATIN_OR_DIGIT.matcher(normalized).replaceAll("-")
                .replaceAll("^-+|-+$", "");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Interest value is not supported: " + rawValue);
        }
        return slug;
    }

    private UserProfileResponse toProfileResponse(UserProfile profile) {
        List<InterestResponse> interests = profile.getInterests().stream()
                .sorted(Comparator.comparing(Interest::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toInterestResponse)
                .toList();
        return new UserProfileResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCompany(),
                profile.getJobTitle(),
                profile.getCity(),
                profile.getAvatarUrl(),
                profile.getBio(),
                interests,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private UserSummaryResponse toSummaryResponse(UserProfile profile) {
        List<InterestResponse> interests = profile.getInterests().stream()
                .sorted(Comparator.comparing(Interest::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toInterestResponse)
                .toList();
        return new UserSummaryResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCompany(),
                profile.getJobTitle(),
                profile.getCity(),
                interests
        );
    }

    private InterestResponse toInterestResponse(Interest interest) {
        return new InterestResponse(interest.getId(), interest.getSlug(), interest.getName());
    }
}
