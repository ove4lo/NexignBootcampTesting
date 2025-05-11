// 3. Исправленный тестовый класс (TestsAddSubscriber.java)
package api;

import api.additionally.AuthSubscriber;
import api.additionally.ManagerTestBase;
import api.additionally.SubscriberData;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestsAddSubscriber extends ManagerTestBase {

    //POST - тест на успешное добавление абонента менеджером
    @Test
    public void addNewSubscriberByManagerTest() {
        SubscriberData newSubscriber = new SubscriberData(
                "79241263778",
                "Алина",
                "Михайлова"
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(newSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(200)
                .body("msisdn", equalTo(newSubscriber.getMsisdn()))
                .body("firstName", equalTo(newSubscriber.getFirstName()))
                .body("surname", equalTo(newSubscriber.getSurname()));
    }

    //POST - тест на добавление абонента менеджером с некорректным номером
    @Test
    public void addSubscriberWithInvalidDataTest() {
        SubscriberData invalidSubscriber = new SubscriberData(
                "792412623",
                "Алина",
                "Михайлова"
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body(emptyString());
    }

    //POST - тест на добавление абонента менеджером без аутентификации
    @Test
    public void addSubscriberUnauthorizedTest() {
        SubscriberData validSubscriber = new SubscriberData(
                "79241263776",
                "Алина",
                "Петрова"
        );

        given()
                .contentType(ContentType.JSON)
                .body(validSubscriber)
                .auth().none() // Отключаем аутентификацию
                .when()
                .post("/subscriber")
                .then()
                .statusCode(401)
                .body(emptyString());
    }

    //POST - тест на добавление абонента менеджером без прав доступа
    @Test
    public void addSubscriberForbiddenTest() {
        SubscriberData validSubscriber = new SubscriberData(
                "79241263776",
                "Алина",
                "Петрова"
        );

        String subscriberToken = AuthSubscriber.getSubscriberToken();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + subscriberToken)
                .body(validSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(403)
                .body("status", equalTo(403))
                .body("error", equalTo("Forbidden"));
    }

    //POST - тест на добавление абонента менеджером с несуществующем тарифом
    @Test
    public void addSubscriberWithInvalidTariffTest() {
        SubscriberData invalidSubscriber = new SubscriberData(
                "79241263770",
                "Иван",
                "Петров"
        );
        invalidSubscriber.setTariffId(0); // Несуществующий ID

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"));
    }

    //POST - тест на добавление существующего абонента менеджером
    @Test
    public void addExistingSubscriberTest() {
        SubscriberData existingSubscriber = new SubscriberData(
                "79241263770",  // Номер, который уже есть в системе
                "Алина",
                "Михайлова"
        );
        existingSubscriber.setBalance(180.0);
        existingSubscriber.setTariffId(2);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(existingSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("error", equalTo("Conflict"));
    }

    //POST - тест на добавление абонента менеджером без обязательного поля
    @Test
    public void addSubscriberWithEmptyRequiredFieldTest() {
        SubscriberData invalidSubscriber = new SubscriberData(
                "79241263770",
                "",
                "Михайлова"
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }
}

