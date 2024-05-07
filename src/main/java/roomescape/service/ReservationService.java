package roomescape.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.app.ReservationAppRequest;
import roomescape.exception.DuplicatedReservationException;
import roomescape.exception.PastReservationException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository) {

        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation save(ReservationAppRequest request) {
        LocalDate date = parseDate(request.date());
        ReservationTime time = findTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Reservation reservation = new Reservation(request.name(), date, time, theme);
        validatePastReservation(date, time);
        validateDuplication(date, request.timeId(), request.themeId());

        return reservationRepository.save(reservation);
    }

    private LocalDate parseDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식을 입력하셨습니다.");
        }
    }

    private ReservationTime findTime(Long timeId) {
        if (timeId == null) {
            throw new IllegalArgumentException("시간 id는 null이 입력될 수 없습니다.");
        }
        try {
            return reservationTimeRepository.findById(timeId);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationTimeNotFoundException();
        }
    }

    private Theme findTheme(Long themeId) {
        if (themeId == null) {
            throw new IllegalArgumentException("테마 id는 null이 입력될 수 없습니다.");
        }
        try {
            return themeRepository.findById(themeId);
        } catch (EmptyResultDataAccessException e) {
            throw new ThemeNotFoundException();
        }
    }

    private void validatePastReservation(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new PastReservationException();
        }
        if (date.isEqual(LocalDate.now()) && time.isBeforeNow()) {
            throw new PastReservationException();
        }
    }

    private void validateDuplication(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.isDuplicated(date, timeId, themeId)) {
            throw new DuplicatedReservationException();
        }
    }

    public int delete(Long id) {
        return reservationRepository.deleteById(id);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}
