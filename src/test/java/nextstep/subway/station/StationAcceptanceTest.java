package nextstep.subway.station;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

    private static final String STATION_DEFAULT_URI = "/stations";

    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // given
        Map<String, String> params = 지하철_역_제공("강남역");

        // when
        ExtractableResponse<Response> response = 지하철_역_생성(params);

        // then
        지하철_역_생성_성공(response);
    }

    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
    @Test
    void createStationWithDuplicateName() {
        // given
        Map<String, String> sameParams = 지하철_역_제공("강남역");
        지하철_역_생성(sameParams);

        // when
        ExtractableResponse<Response> response = 지하철_역_생성(sameParams);

        // then
        지하철_역_생성_실패(response);
    }

    @DisplayName("지하철역 목록을 조회한다.")
    @Test
    void getStations() {
        /// given
        Map<String, String> params1 = 지하철_역_제공("강남역");
        ExtractableResponse<Response> createResponse1 = 지하철_역_생성(params1);

        Map<String, String> params2 = 지하철_역_제공("역삼역");
        ExtractableResponse<Response> createResponse2 = 지하철_역_생성(params2);

        // when
        ExtractableResponse<Response> response = 지하철_역_조회();

        // then
        지하철_역_응답_성공(response);
        List<Long> expectedLineIds = 지하철_역_목록_예상(new ArrayList<>(Arrays.asList(createResponse1, createResponse2)));
        List<Long> resultLineIds = 지하철_역_목록_응답(response);
        지하철_역_목록_일치_성공(resultLineIds, expectedLineIds);
    }

    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStation() {
        // given
        Map<String, String> params = 지하철_역_제공("강남역");
        ExtractableResponse<Response> createResponse = 지하철_역_생성(params);

        // when
        ExtractableResponse<Response> response = 지하철_역_삭제(createResponse);

        // then
        지하철_역_삭제_성공(response);
    }


    static Map<String, String> 지하철_역_제공(String name) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        return params;
    }

    static ExtractableResponse<Response> 지하철_역_생성(Map<String, String> params) {
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(STATION_DEFAULT_URI)
                .then().log().all()
                .extract();
    }

    static ExtractableResponse<Response> 지하철_역_조회() {
        return RestAssured.given().log().all()
                .when()
                .get(STATION_DEFAULT_URI)
                .then().log().all()
                .extract();
    }

    static ExtractableResponse<Response> 지하철_역_삭제(ExtractableResponse<Response> createResponse) {
        String uri = createResponse.header("Location");
        return RestAssured.given().log().all()
                .when()
                .delete(uri)
                .then().log().all()
                .extract();
    }

    static List<Long> 지하철_역_목록_예상(ArrayList<ExtractableResponse<Response>> responses) {
        return responses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());
    }

    static List<Long> 지하철_역_목록_응답(ExtractableResponse<Response> response) {
        return response.jsonPath().getList(".", StationResponse.class).stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());
    }

    static void 지하철_역_생성_성공(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    static void 지하철_역_생성_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.statusCode()).isNotEqualTo(HttpStatus.CREATED.value());
    }

    static void 지하철_역_응답_성공(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    static void 지하철_역_목록_일치_성공(List<Long> resultLineIds, List<Long> expectedLineIds) {
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    static void 지하철_역_삭제_성공(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }


}
