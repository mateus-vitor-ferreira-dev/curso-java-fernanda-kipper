package com.eventostec.api.repositories;

import com.eventostec.api.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA de {@link Coupon}.
 */
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    /**
     * Retorna os cupons de um evento (query derivada pelo nome do método).
     *
     * @param eventId id do evento
     * @return os cupons do evento
     */
    List<Coupon> findByEventId(UUID eventId);
}
