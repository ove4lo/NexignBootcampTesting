// 2. Модифицированный AuthManager с улучшенной обработкой ошибок
package api.additionally;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

public class AuthManager {
    private static final String AUTH_URL = "http://localhost:8080/sso/auth/login";
    private static String managerToken;

    public static String getManagerToken() {
        if (managerToken == null) {
            try {

                String authBody = "{"
                        + "\"msisdn\": \"-77777777777\","
                        + "\"role\": \"ROLE_MANAGER\""
                        + "}";

                Response response = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(authBody)
                        .log().all()
                        .when()
                        .post(AUTH_URL);

                if (response.getStatusCode() != 200) {
                    throw new RuntimeException("Ошибка авторизации! Код: " + response.getStatusCode());
                }

                managerToken = response.jsonPath().getString("token");

                if (managerToken == null || managerToken.isEmpty()) {
                    throw new RuntimeException("Токен не найден в ответе!");
                }

            } catch (Exception e) {
                System.err.println("\n!!! ОШИБКА ПОЛУЧЕНИЯ ТОКЕНА !!!");
                e.printStackTrace();
                throw new RuntimeException("Fatal: Не удалось получить токен", e);
            }
        }
        return managerToken;
    }
}