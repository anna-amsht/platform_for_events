package com.unik.authservice.client;

import com.unik.authservice.dto.internal.UpsertUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserServiceClient {

    @PostMapping
    void upsertUserProfile(@RequestBody UpsertUserProfileRequest request);
}
