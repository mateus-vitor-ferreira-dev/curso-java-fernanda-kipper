package com.eventostec.api.domain.coupon;

/**
 * Dados de entrada para cadastrar um cupom em um evento.
 *
 * @param code       código do cupom
 * @param discount   percentual de desconto
 * @param validUntil validade do cupom em epoch millis
 */
public record CouponRequestDTO(String code, Integer discount, Long validUntil){
}
