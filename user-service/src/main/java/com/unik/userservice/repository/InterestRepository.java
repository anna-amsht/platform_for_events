package com.unik.userservice.repository;

import com.unik.userservice.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

    List<Interest> findAllBySlugIn(Collection<String> slugs);
}
