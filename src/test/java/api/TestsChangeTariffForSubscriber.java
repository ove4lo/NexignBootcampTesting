package api;

import api.additionally.AuthSubscriber;
import api.additionally.ManagerTestBase;
import api.additionally.SubscriberData;
import api.additionally.TestData;
import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestsChangeTariffForSubscriber extends ManagerTestBase {

    // PUT - успешное изменение тарифа менеджером
    @Test
    public void changeTariffForSubscriberByManagerTest() {
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        int newTariffId = 1;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .pathParam("tariffId", newTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(200)
                .body("tariffId", equalTo(newTariffId))
                .body("msisdn", equalTo(validSubscriber.getMsisdn()));
    }

    // PUT - попытка изменения с невалидным номером
    @Test
    public void changeTariffForSubscriberByManagerWithInvalidNumberTest() {
        SubscriberData invalidSubscriber = TestData.createInvalidPhoneSubscriber();
        int newTariffId = 12;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", invalidSubscriber.getSubscriberId())
                .pathParam("tariffId", newTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }

    // PUT - попытка изменения тарифа абонентом
    @Test
    public void changeTariffForSubscriberBySelfTest() {
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        int newTariffId = 11;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AuthSubscriber.getSubscriberToken())
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .pathParam("tariffId", newTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(403)
                .body("status", equalTo(403))
                .body("error", equalTo("Forbidden"));
    }

    // PUT - изменение тарифа несуществующему абоненту
    @Test
    public void changeTariffForNotSubscriberByManagerTest() {
        SubscriberData invalidSubscriber = TestData.nonExistSubscriber();
        int newTariffId = 11;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", invalidSubscriber.getSubscriberId())
                .pathParam("tariffId", newTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"));
    }

    // PUT - изменение на несуществующий тариф
    @Test
    public void changeTariffOnInvalidForSubscriberByManagerTest() {
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        int nonExistentTariffId = 999;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .pathParam("tariffId", nonExistentTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"));
    }

    // PUT - изменение без авторизации
    @Test
    public void changeTariffWithoutAuthorizationTest() {
        SubscriberData validSubscriber = TestData.createValidSubscriber();
        int newTariffId = 2;

        given()
                .contentType(ContentType.JSON)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .pathParam("tariffId", newTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(401);
    }

}