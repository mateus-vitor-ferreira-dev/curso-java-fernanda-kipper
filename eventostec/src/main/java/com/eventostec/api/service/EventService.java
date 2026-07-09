package com.eventostec.api.service;

import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.CouponRepository;
import com.eventostec.api.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Regras de negócio de eventos: criação (com upload de imagem no S3 e endereço),
 * listagem paginada de eventos futuros, filtro e montagem do detalhe com cupons.
 */
@Service
public class EventService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private EventRepository repository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CouponRepository couponRepository;

    /**
     * Cria e persiste um evento. Se houver imagem, faz upload no S3; se não for
     * remoto, também cria o endereço a partir de cidade/estado.
     *
     * @param data dados do evento
     * @return o evento criado
     */
    public Event createEvent(EventRequestDTO data){
        String imgUrl = null;

        if(data.image() != null) {
            imgUrl = this.uploadImg(data.image());
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());
        newEvent.setDate(new Date(data.date()));
        newEvent.setRemote(data.remote());

        newEvent.setImgUrl(imgUrl);

        repository.save(newEvent);

        if(!data.remote()) {
            this.addressService.createAddress(data, newEvent);
        }

        return newEvent;
    }

    /**
     * Lista, paginado, os eventos com data maior ou igual a agora.
     *
     * @param page índice da página (base 0)
     * @param size itens por página
     * @return os eventos futuros da página
     */
    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Event> eventsPage = this.repository.findUpcomingEvents(new Date(), pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() : "",
                event.isRemote(),
                event.getEventUrl(),
                event.getImgUrl()
        )).stream().toList();
    }

    /**
     * Lista eventos futuros aplicando filtros opcionais (título, cidade, UF,
     * presencial/remoto e intervalo de datas). Filtros nulos são neutralizados.
     *
     * @return os eventos que atendem aos filtros
     */
    public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf, Boolean remote, Date startDate, Date endDate) {

        title     = (title != null)     ? title : "";
        city      = (city != null)      ? city : "";
        uf        = (uf != null)        ? uf : "";
        startDate = (startDate != null) ? startDate : new Date(0);
        endDate   = (endDate != null)   ? endDate : new Date(253402214400000L); // ~ano 9999: sem limite superior
        // remote fica nullable de propósito: null = "Todos" (não filtra por presencial/remoto)

        Pageable pageable = PageRequest.of(page,size);

        Page<Event> eventsPage = this.repository.findFilteredEvents(new Date(), title, city, uf, remote, startDate, endDate, pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() : "",
                event.isRemote(),
                event.getEventUrl(),
                event.getImgUrl()
        )).stream().toList();
    }

    /**
     * Busca um evento pelo id e monta seu detalhe, incluindo os cupons.
     *
     * @param id identificador do evento
     * @return o detalhe do evento com a lista de cupons
     */
    public EventDetailsDTO getEventById(UUID id) {
        Event event = this.repository.findById(id).orElseThrow();

        // 1. busca os cupons desse evento (query derivada pelo nome do método)
        List<Coupon> coupons = this.couponRepository.findByEventId(id);

        // 2. converte cada Coupon (entidade) em CouponDTO (só os campos que queremos expor)
        List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream()
                .map(coupon -> new EventDetailsDTO.CouponDTO(
                        coupon.getCode(),
                        coupon.getDiscount(),
                        coupon.getValid()))
                .toList();

        // 3. monta o detalhe do evento já com a lista de cupons dentro
        return new EventDetailsDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() : "",
                event.isRemote(),
                event.getEventUrl(),
                event.getImgUrl(),
                couponDTOs
        );
    }

    private String uploadImg(MultipartFile multiparteFile){
        String filename = UUID.randomUUID() + "-" + multiparteFile.getOriginalFilename();
        try {
            File file = this.convertMultipartToFile(multiparteFile);
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(filename).build(),
                    RequestBody.fromFile(file));
            file.delete();
            return s3Client.utilities()
                    .getUrl(GetUrlRequest.builder().bucket(bucketName).key(filename).build())
                    .toString();
        } catch (Exception e) {
            System.out.println("Erro ao subir arquivo");
            return null;
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return convFile;
    }

}
