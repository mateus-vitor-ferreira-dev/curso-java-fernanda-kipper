package com.eventostec.api.service;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
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

@Service
public class EventService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private EventRepository repository;

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

        return newEvent;
    }

    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Event> eventsPage = this.repository.findUpcomingEvents(new Date(), pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                "",
                "",
                event.isRemote(),
                event.getEventUrl(),
                event.getImgUrl()
        )).stream().toList();
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
