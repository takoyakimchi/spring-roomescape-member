package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.app.ThemeAppRequest;
import roomescape.exception.DuplicatedThemeException;
import roomescape.exception.ReservationExistsException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int MAX_POPULAR_THEME_COUNT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Theme save(ThemeAppRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        validateDuplication(request);
        return themeRepository.save(theme);
    }

    private void validateDuplication(ThemeAppRequest request) {
        if (themeRepository.isNameExists(request.name())) {
            throw new DuplicatedThemeException();
        }
    }

    public int delete(Long id) {
        if (reservationRepository.isThemeIdExists(id)) {
            throw new ReservationExistsException();
        }
        return themeRepository.deleteById(id);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findPopular() {
        return themeRepository.findPopular(MAX_POPULAR_THEME_COUNT);
    }
}
