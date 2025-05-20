package api;

import api.additionally.AuthSubscriber;
import api.additionally.ManagerTestBase;
import api.additionally.SubscriberData;
import api.additionally.TestData;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestsAddSubscriber extends ManagerTestBase {

    //POST - тест на успешное добавление абонента менеджером
    @Test
    public void addNewSubscriberByManagerTest() {
        SubscriberData validSubscriber = TestData.createValidSubscriber();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(validSubscriber)
                .when()
                .post("/subscriber")
                .then()
                .statusCode(200)
                .body("msisdn", equalTo(validSubscriber.getMsisdn()))
                .body("firstName", equalTo(validSubscriber.getFirstName()))
                .body("surname", equalTo(validSubscriber.getSurname()));
    }

    //POST - тест на добавление абонента менеджером с некорректным номером
    @Test
    public void addSubscriberWithInvalidDataTest() {
        SubscriberData invalidSubscriber = TestData.createInvalidPhoneSubscriber();

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
        SubscriberData validSubscriber = TestData.createValidSubscriber();

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
        SubscriberData validSubscriber = TestData.createValidSubscriber();

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
        SubscriberData invalidSubscriber = TestData.createSubscriberWithInvalidTariff();

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
        SubscriberData existingSubscriber = TestData.createExistingSubscriber();

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
        SubscriberData invalidSubscriber = TestData.createSubscriberWithEmptyName();

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

