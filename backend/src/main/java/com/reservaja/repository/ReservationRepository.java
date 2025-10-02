package com.reservaja.repository;

import com.reservaja.model.entity.Reservation;
import com.reservaja.model.entity.Room;
import com.reservaja.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByUser(User user);

    List<Reservation> findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(
        Room room, LocalDateTime endTime, LocalDateTime startTime 
    );
}
