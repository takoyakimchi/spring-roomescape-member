package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.FindThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class UserThemeController {

    private final ThemeService themeService;

    public UserThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<FindThemeResponse>> findAll() {
        List<FindThemeResponse> response = themeService.findAll()
            .stream()
            .map(theme -> new FindThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail())
            ).toList();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FindThemeResponse>> findPopular() {
        List<FindThemeResponse> response = themeService.findPopular()
            .stream()
            .map(theme -> new FindThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail())
            ).toList();

        return ResponseEntity.ok().body(response);
    }
}