package com.eventostec.api.controller;

import java.util.UUID;

import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.coupon.CouponRequestDTO;
import com.eventostec.api.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoints REST de cupons de desconto vinculados a eventos.
 */
@Tag(name = "Cupons", description = "Cadastro de cupons de desconto em eventos")
@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * Cadastra um cupom de desconto para um evento existente.
     *
     * @param eventId identificador (UUID) do evento
     * @param data    dados do cupom (código, desconto e validade)
     * @return o cupom criado
     */
    @Operation(summary = "Cadastra um cupom em um evento")
    @PostMapping("/event/{eventId}")
    public ResponseEntity<Coupon> addCouponToEvent(@PathVariable UUID eventId, @RequestBody CouponRequestDTO data) {
        Coupon coupon = this.couponService.addCouponToEvent(eventId, data);
        return ResponseEntity.ok(coupon);
    }
}
