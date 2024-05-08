package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> reservationMapper;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        this.simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
            .withTableName("reservation")
            .usingGeneratedKeyColumns("id");

        this.reservationMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("reservation_name"),
            rs.getString("reservation_date"),
            new ReservationTime(
                rs.getLong("time_id"),
                rs.getString("time_value")
            ),
            new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
            )
        );
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> saveSource = Map.ofEntries(
            Map.entry("name", reservation.getName()),
            Map.entry("date", reservation.getDate()),
            Map.entry("time_id", reservation.getTime().getId()),
            Map.entry("theme_id", reservation.getTheme().getId())
        );
        long id = simpleJdbcInsert.executeAndReturnKey(saveSource).longValue();
        return findById(id);
    }

    private Reservation findById(Long id) {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name AS reservation_name,
                r.date AS reservation_date,
                t.id AS time_id,
                t.start_at AS time_value,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail
            FROM reservation AS r
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            WHERE r.id = ?
            """;

        return jdbcTemplate.queryForObject(sql, reservationMapper, id);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name AS reservation_name,
                r.date AS reservation_date,
                t.id AS time_id,
                t.start_at AS time_value,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail
            FROM reservation AS r
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            """;

        return jdbcTemplate.query(sql, reservationMapper);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean isTimeIdUsed(Long id) {
        String sql = "SELECT EXISTS (SELECT id FROM reservation WHERE time_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public Boolean isThemeIdUsed(Long id) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public Boolean isDuplicated(String rawDate, Long timeId, Long themeId) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, rawDate, timeId, themeId);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name AS reservation_name,
                r.date AS reservation_date,
                t.id AS time_id,
                t.start_at AS time_value,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail
            FROM reservation AS r
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            WHERE date = ? AND theme_id = ?
            """;

        return jdbcTemplate.query(sql, reservationMapper, date, themeId);
    }
}
