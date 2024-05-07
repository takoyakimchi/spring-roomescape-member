package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    int deleteById(Long id);

    boolean isTimeIdExists(Long id);

    boolean isThemeIdExists(Long id);

    boolean isDuplicated(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);
}
