package com.unik.userservice.repository;

import com.unik.userservice.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Override
    @EntityGraph(attributePaths = "interests")
    Optional<UserProfile> findById(UUID id);

    @Query(
            value = """
                    select distinct u from UserProfile u
                    left join u.interests i
                    where (:query is null
                        or lower(u.email) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.firstName, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.lastName, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.company, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.jobTitle, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.city, '')) like lower(concat('%', :query, '%')))
                      and (:interestSlug is null or i.slug = :interestSlug)
                    """,
            countQuery = """
                    select count(distinct u) from UserProfile u
                    left join u.interests i
                    where (:query is null
                        or lower(u.email) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.firstName, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.lastName, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.company, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.jobTitle, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(u.city, '')) like lower(concat('%', :query, '%')))
                      and (:interestSlug is null or i.slug = :interestSlug)
                    """
    )
    Page<UserProfile> search(
            @Param("query") String query,
            @Param("interestSlug") String interestSlug,
            Pageable pageable
    );
}
