package com.eventostec.api.repositories;

import com.eventostec.api.domain.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.UUID;

/**
 * Repositório JPA de {@link Event}, com consultas de eventos futuros e filtro.
 */
public interface EventRepository extends JpaRepository<Event, UUID> {

        /**
     * Eventos com data maior ou igual à informada (eventos futuros), já
     * carregando o endereço (fetch join).
     *
     * @param currentDate data de corte (normalmente agora)
     * @param pageable    paginação
     * @return a página de eventos futuros
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.address a WHERE e.date >= :currentDate")
        public Page<Event> findUpcomingEvents(@Param("currentDate") Date currentDate, Pageable pageable);

        /**
     * Eventos futuros aplicando filtros opcionais. Parâmetros vazios/nulos são
     * neutralizados na própria query (LIKE '%%' e COALESCE para o boolean remote).
     *
     * @return a página de eventos filtrados
     */
    @Query("SELECT e FROM Event e " +
            "LEFT JOIN e.address a " +
            "WHERE e.date >= :currentDate " +
            "AND e.title LIKE %:title% " +
            "AND (a.city LIKE %:city% OR :city = '') " +
            "AND (a.uf LIKE %:uf% OR :uf = '') " +
            "AND e.remote = COALESCE(:remote, e.remote) " +
            "AND e.date >= :startDate " +
            "AND e.date <= :endDate")
        Page<Event> findFilteredEvents(@Param("currentDate") Date date,
                                       @Param("title") String title,
                                       @Param("city") String city,
                                       @Param("uf") String uf,
                                       @Param("remote") Boolean remote,
                                       @Param("startDate") Date startDate,
                                       @Param("endDate") Date endDate,
                                       Pageable pageable);
}

