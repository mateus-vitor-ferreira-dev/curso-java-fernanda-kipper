package com.eventostec.api.controller;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoints REST de eventos: criação, listagem paginada de eventos futuros,
 * filtro e detalhe (com cupons).
 */
@Tag(name = "Eventos", description = "Criação, listagem, filtro e detalhe de eventos")
@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Cria um novo evento. Recebe {@code multipart/form-data} para suportar o
     * upload opcional de imagem (enviada ao S3). Eventos presenciais também
     * ganham um endereço a partir de {@code city}/{@code state}.
     *
     * @return o evento criado
     */
    @Operation(summary = "Cria um evento", description = "Recebe multipart/form-data; a imagem é opcional e enviada ao S3.")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Event> create(@RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("date") Long date,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("remote") Boolean remote,
            @RequestParam("eventUrl") String eventUrl,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        EventRequestDTO eventRequestDTO = new EventRequestDTO(title, description, date, city, state, remote, eventUrl,
                image);
        Event newEvent = this.eventService.createEvent(eventRequestDTO);
        return ResponseEntity.ok(newEvent);

    }

    /**
     * Lista, de forma paginada, os eventos com data igual ou posterior a agora.
     *
     * @param page índice da página (começa em 0)
     * @param size quantidade de itens por página (obrigatório)
     * @return a página de eventos futuros
     */
    @Operation(summary = "Lista eventos futuros (paginado)")
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getEvents(@RequestParam(defaultValue = "0") int page,
            @RequestParam int size) {
        List<EventResponseDTO> allEvents = this.eventService.getUpcomingEvents(page, size);
        return ResponseEntity.ok(allEvents);
    }

    /**
     * Filtra eventos futuros por título, cidade, UF, presencial/remoto e
     * intervalo de datas. Todos os filtros são opcionais.
     *
     * @param remote {@code true} só remotos, {@code false} só presenciais, ausente para ambos
     * @return os eventos que atendem aos filtros
     */
    @Operation(summary = "Filtra eventos futuros", description = "Todos os parâmetros de filtro são opcionais.")
    @GetMapping("/filter")
    public ResponseEntity<List<EventResponseDTO>> filterEvents(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<EventResponseDTO> events = eventService.getFilteredEvents(page, size, title, city, uf, remote, startDate,
                endDate);
        return ResponseEntity.ok(events);
    }

    /**
     * Retorna os detalhes de um evento pelo seu id, incluindo os cupons cadastrados.
     *
     * @param id identificador (UUID) do evento
     * @return os detalhes do evento com a lista de cupons
     */
    @Operation(summary = "Detalha um evento por id", description = "Inclui a lista de cupons do evento.")
    @GetMapping("/{id}")
    public ResponseEntity<EventDetailsDTO> getEventById(@PathVariable UUID id) {
        EventDetailsDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}
