package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stock_order.infrastructure.persistence.entity.UserAddressEntity;

import jakarta.transaction.Transactional;

public interface SpringDataUserAddressJpaRepository extends JpaRepository<UserAddressEntity, Long> {

    List<UserAddressEntity> findByUserId(Long userId);

    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from UserAddressEntity a where a.id = :id and a.userId = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update UserAddressEntity a set a.isDefault = false where a.userId = :userId and a.id <> :keepId")
    int unsetDefaultForOthers(@Param("userId") Long userId, @Param("keepId") Long keepId);}
