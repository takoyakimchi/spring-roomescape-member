package roomescape.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Reservation;
import roomescape.dto.app.ReservationAppRequest;
import roomescape.dto.web.ReservationTimeWebResponse;
import roomescape.dto.web.ReservationWebRequest;
import roomescape.dto.web.ReservationWebResponse;
import roomescape.dto.web.ThemeWebResponse;
import roomescape.exception.DuplicatedReservationException;
import roomescape.exception.PastReservationException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationWebResponse> reserve(@RequestBody ReservationWebRequest request) {
        Reservation newReservation = reservationService.save(
            new ReservationAppRequest(request.name(), request.date(), request.timeId(), request.themeId()));
        Long id = newReservation.getId();

        ReservationWebResponse reservationWebResponse = new ReservationWebResponse(id, newReservation.getName(),
            newReservation.getDate(),
            ReservationTimeWebResponse.from(newReservation),
            ThemeWebResponse.from(newReservation));

        return ResponseEntity.created(URI.create("/reservations/" + id))
            .body(reservationWebResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationWebResponse>> getReservations() {
        List<Reservation> reservations = reservationService.findAll();
        List<ReservationWebResponse> reservationWebResponse = reservations.stream().
            map(reservation -> new ReservationWebResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeWebResponse.from(reservation),
                ThemeWebResponse.from(reservation)
            )).toList();

        return ResponseEntity.ok(reservationWebResponse);
    }


    @ExceptionHandler(ReservationTimeNotFoundException.class)
    public ResponseEntity<String> handleReservationTimeNotFoundException(ReservationTimeNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ThemeNotFoundException.class)
    public ResponseEntity<String> handleThemeNotFoundException(ThemeNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<String> handlePastReservationException(PastReservationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DuplicatedReservationException.class)
    public ResponseEntity<String> handleDuplicatedReservationException(DuplicatedReservationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
