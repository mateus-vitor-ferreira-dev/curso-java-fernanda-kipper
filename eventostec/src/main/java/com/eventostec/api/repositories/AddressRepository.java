package com.eventostec.api.repositories;

import com.eventostec.api.domain.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório JPA de {@link Address}.
 */
public interface AddressRepository extends JpaRepository<Address, UUID> {
}
