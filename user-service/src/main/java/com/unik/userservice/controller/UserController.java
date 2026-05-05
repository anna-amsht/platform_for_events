package com.unik.userservice.controller;

import com.unik.userservice.dto.request.UpdateMyProfileRequest;
import com.unik.userservice.dto.response.InterestResponse;
import com.unik.userservice.dto.response.PagedResponse;
import com.unik.userservice.dto.response.UserProfileResponse;
import com.unik.userservice.dto.response.UserSummaryResponse;
import com.unik.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@Validated
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return userProfileService.getMyProfile(currentUserId(authentication));
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateMyProfileRequest request) {
        return userProfileService.updateMyProfile(currentUserId(authentication), request);
    }

    @GetMapping("/search")
    public PagedResponse<UserSummaryResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return userProfileService.search(query, interest, page, size);
    }

    @GetMapping("/interests")
    public List<InterestResponse> interests() {
        return userProfileService.getAllInterests();
    }

    @GetMapping("/{userId}")
    public UserProfileResponse getProfile(@PathVariable UUID userId) {
        return userProfileService.getProfile(userId);
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getDetails();
    }
}
