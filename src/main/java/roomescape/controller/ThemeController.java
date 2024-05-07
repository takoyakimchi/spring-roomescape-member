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
import roomescape.domain.Theme;
import roomescape.dto.app.ThemeAppRequest;
import roomescape.dto.web.ErrorMessageResponse;
import roomescape.dto.web.ThemeWebRequest;
import roomescape.dto.web.ThemeWebResponse;
import roomescape.exception.DuplicatedThemeException;
import roomescape.exception.ReservationExistsException;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeWebResponse> create(@RequestBody ThemeWebRequest request) {
        Theme newTheme = themeService.save(
            new ThemeAppRequest(request.name(), request.description(), request.thumbnail()));
        Long id = newTheme.getId();

        return ResponseEntity.created(URI.create("/themes/" + id))
            .body(new ThemeWebResponse(
                id,
                newTheme.getName(),
                newTheme.getDescription(),
                newTheme.getThumbnail()
            ));
    }

    @GetMapping
    public ResponseEntity<List<ThemeWebResponse>> findAll() {
        List<ThemeWebResponse> response = themeService.findAll()
            .stream()
            .map(theme -> new ThemeWebResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail())
            ).toList();

        return ResponseEntity.ok()
            .body(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeWebResponse>> findPopular() {
        List<ThemeWebResponse> response = themeService.findPopular()
            .stream()
            .map(theme -> new ThemeWebResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail())
            ).toList();

        return ResponseEntity.ok()
            .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicatedThemeException.class)
    public ResponseEntity<ErrorMessageResponse> handleDuplicatedThemeException(DuplicatedThemeException e) {
        ErrorMessageResponse response = new ErrorMessageResponse(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ReservationExistsException.class)
    public ResponseEntity<ErrorMessageResponse> handleReservationExistsException(ReservationExistsException e) {
        ErrorMessageResponse response = new ErrorMessageResponse(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
