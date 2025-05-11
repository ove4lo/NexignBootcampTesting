// 2. Модифицированный AuthManager с улучшенной обработкой ошибок
package api.additionally;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

public class AuthSubscriber {
    private static final String AUTH_URL = "http://localhost:8080/sso/auth/login";
    private static String subscriberToken;

    public static String getSubscriberToken() {
        if (subscriberToken == null) {
            try {

                String authBody = "{"
                        + "\"msisdn\": \"79000000001\","
                        + "\"role\": \"ROLE_SUBSCRIBER\""
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

                subscriberToken = response.jsonPath().getString("token");

                if (subscriberToken== null || subscriberToken.isEmpty()) {
                    throw new RuntimeException("Токен не найден в ответе!");
                }

            } catch (Exception e) {
                System.err.println("\n!!! ОШИБКА ПОЛУЧЕНИЯ ТОКЕНА !!!");
                e.printStackTrace();
                throw new RuntimeException("Fatal: Не удалось получить токен", e);
            }
        }
        return subscriberToken;
    }
}