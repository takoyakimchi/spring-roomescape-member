package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationTime;
import roomescape.dto.app.ReservationTimeAppRequest;
import roomescape.dto.app.ReservationTimeAppResponse;
import roomescape.dto.web.ErrorMessageResponse;
import roomescape.dto.web.ReservationTimeUserWebResponse;
import roomescape.dto.web.ReservationTimeWebRequest;
import roomescape.dto.web.ReservationTimeWebResponse;
import roomescape.exception.DuplicatedReservationTimeException;
import roomescape.exception.ReservationExistsException;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeWebResponse> create(@RequestBody ReservationTimeWebRequest request) {
        ReservationTime newReservationTime = reservationTimeService.save(
            new ReservationTimeAppRequest(request.startAt()));
        Long id = newReservationTime.getId();

        return ResponseEntity.created(URI.create("/times/" + id))
            .body(new ReservationTimeWebResponse(
                id,
                newReservationTime.getStartAt()
            ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeWebResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();
        List<ReservationTimeWebResponse> reservationTimeWebResponses = reservationTimes.stream()
            .map(reservationTime -> new ReservationTimeWebResponse(reservationTime.getId(),
                reservationTime.getStartAt()))
            .toList();

        return ResponseEntity.ok(reservationTimeWebResponses);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReservationTimeUserWebResponse>> getReservationTimesWithAvailability(
        @RequestParam LocalDate date, @RequestParam Long id) {

        List<ReservationTimeAppResponse> appResponses = reservationTimeService
            .findAllWithBookAvailability(date, id);
        List<ReservationTimeUserWebResponse> webResponses = appResponses.stream()
            .map(response -> new ReservationTimeUserWebResponse(
                response.id(),
                response.startAt(),
                response.alreadyBooked())
            ).toList();

        return ResponseEntity.ok(webResponses);
    }

    @ExceptionHandler(ReservationExistsException.class)
    public ResponseEntity<ErrorMessageResponse> handleReservationExistsException(ReservationExistsException e) {
        ErrorMessageResponse response = new ErrorMessageResponse(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicatedReservationTimeException.class)
    public ResponseEntity<ErrorMessageResponse> handleDuplicatedReservationTimeException(DuplicatedReservationTimeException e) {
        ErrorMessageResponse response = new ErrorMessageResponse(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
