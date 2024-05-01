package roomescape.domain;

import java.util.List;

public interface ThemeRepository {

    Theme save(Theme theme);

    int deleteById(Long id);

    List<Theme> findAll();

    Theme findById(Long id);

    Long countByName(String name);
}