package com.eventostec.api.domain.event;

import java.util.Date;
import java.util.UUID;

/**
 * Representação de um evento usada nas listagens (paginação e filtro), sem cupons.
 */
public record EventResponseDTO(UUID id, String title, String description, Date date, String city, String state, Boolean remote, String eventUrl, String imageUrl) {
}
