package roomescape.controller.api;

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
import roomescape.controller.dto.CreateThemeRequest;
import roomescape.controller.dto.CreateThemeResponse;
import roomescape.controller.dto.ErrorMessageResponse;
import roomescape.domain.Theme;
import roomescape.exception.DuplicatedThemeException;
import roomescape.exception.ReservationExistsException;
import roomescape.service.ThemeService;
import roomescape.service.dto.SaveThemeDto;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<CreateThemeResponse> create(@RequestBody CreateThemeRequest request) {
        Theme newTheme = themeService.save(
            new SaveThemeDto(request.name(), request.description(), request.thumbnail()));
        Long id = newTheme.getId();

        return ResponseEntity.created(URI.create("/themes/" + id))
            .body(new CreateThemeResponse(
                id,
                newTheme.getName(),
                newTheme.getDescription(),
                newTheme.getThumbnail()
            ));
    }

    @GetMapping
    public ResponseEntity<List<CreateThemeResponse>> findAll() {
        List<CreateThemeResponse> response = themeService.findAll()
            .stream()
            .map(theme -> new CreateThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail())
            ).toList();

        return ResponseEntity.ok()
            .body(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CreateThemeResponse>> findPopular() {
        List<CreateThemeResponse> response = themeService.findPopular()
            .stream()
            .map(theme -> new CreateThemeResponse(
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