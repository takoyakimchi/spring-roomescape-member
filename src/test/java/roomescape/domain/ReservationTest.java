package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.reservation.IllegalReservationFormatException;

class ReservationTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new Reservation(1L, "wiib", LocalDate.now(), new ReservationTime(1L, LocalTime.now()),
            new Theme("방탈출", "방탈출하는 게임", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")))
            .doesNotThrowAnyException();
        assertThatCode(() -> new Reservation("wiib", LocalDate.now(), new ReservationTime(1L, LocalTime.now()),
            new Theme("방탈출", "방탈출하는 게임", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")))
            .doesNotThrowAnyException();

    }

    @DisplayName("빈 이름으로 생성하면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void create_WithBlankName(String name) {
        assertThatThrownBy(() -> new Reservation(name, LocalDate.MAX, new ReservationTime(LocalTime.MAX),
            new Theme("방탈출", "방탈출하는 게임", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")))
            .isInstanceOf(IllegalReservationFormatException.class);
    }
}
