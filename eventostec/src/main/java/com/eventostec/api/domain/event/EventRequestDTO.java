package com.eventostec.api.domain.event;

import org.springframework.web.multipart.MultipartFile;

/**
 * Dados de entrada para criar um evento (recebidos como multipart/form-data).
 * A imagem é opcional e, quando enviada, vai para o S3.
 */
public record EventRequestDTO(String title, String description, Long date, String city, String state, Boolean remote, String eventUrl, MultipartFile image) {
}
