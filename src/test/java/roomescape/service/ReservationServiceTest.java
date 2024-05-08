package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.SaveReservationDto;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String name = "tre";
    private final String rawDate = "2060-01-01";
    private final Long timeId = 1L;
    private final Long themeId = 1L;

    private final ReservationTime reservationTime = new ReservationTime(1L, "10:00");
    private final Theme theme = new Theme(1L, "theme1", "desc1", "https://");

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES ('theme1', 'desc1', 'https://')");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('10:00')");
    }

    @DisplayName("성공: 예약을 저장하고, 해당 예약을 id값과 함께 반환한다.")
    @Test
    void save() {
        Reservation saved = reservationService.save(new SaveReservationDto(name, rawDate, timeId, themeId));
        assertThat(saved)
            .isEqualTo(new Reservation(saved.getId(), name, rawDate, reservationTime, theme));
    }

    @DisplayName("실패: 빈 이름을 저장하면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void save_IllegalName(String invalidName) {
        assertThatThrownBy(
            () -> reservationService.save(new SaveReservationDto(invalidName, rawDate, timeId, themeId))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("예약자 이름은 null이거나 비어 있을 수 없습니다.");
    }

    @DisplayName("실패: 존재하지 않는 날짜 입력 시 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"2030-13-01", "2030-12-32"})
    void save_IllegalDate(String invalidRawDate) {
        assertThatThrownBy(
            () -> reservationService.save(new SaveReservationDto(name, invalidRawDate, timeId, themeId))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("잘못된 날짜 형식입니다.");
    }

    @DisplayName("실패: 존재하지 않는 시간 ID 입력 시 예외가 발생한다.")
    @Test
    void save_TimeIdDoesntExist() {
        assertThatThrownBy(
            () -> reservationService.save(new SaveReservationDto(name, rawDate, 2L, themeId))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다.");
    }

    @DisplayName("실패: 중복 예약을 생성하면 예외가 발생한다.")
    @Test
    void save_Duplication() {
        reservationService.save(new SaveReservationDto(name, rawDate, timeId, themeId));

        assertThatThrownBy(
            () -> reservationService.save(new SaveReservationDto(name, rawDate, timeId, themeId))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간에 예약이 이미 존재합니다.");
    }

    @DisplayName("실패: 과거 날짜 예약 생성하면 예외 발생 -- 어제")
    @Test
    void save_PastDateReservation() {
        String yesterday = LocalDate.now().minusDays(1).toString();

        assertThatThrownBy(
            () -> reservationService.save(new SaveReservationDto(name, yesterday, timeId, themeId))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }

    @DisplayName("실패: 같은 날짜, 과거 시간 예약 생성하면 예외 발생 -- 1분 전")
    @Test
    void save_TodayPastTimeReservation() {
        String today = LocalDate.now().toString();
        String oneMinuteAgo = LocalTime.now().minusMinutes(1).toString();

        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(oneMinuteAgo));

        assertThatThrownBy(
            () -> reservationService.save(new SaveReservationDto(name, today, savedTime.getId(), themeId))
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("과거 예약을 추가할 수 없습니다.");
    }
}
