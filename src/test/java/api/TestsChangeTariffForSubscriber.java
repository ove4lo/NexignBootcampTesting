package api;

import api.additionally.AuthSubscriber;
import api.additionally.ManagerTestBase;
import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestsChangeTariffForSubscriber extends ManagerTestBase {

    // PUT - успешное изменение тарифа менеджером
    @Test
    public void changeTariffForSubscriberByManagerTest() {
        String validMsisdn = "79241263770";
        int newTariffId = 1;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validMsisdn)
                .pathParam("tariffId", newTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(200)
                .body("tariffId", equalTo(newTariffId))
                .body("msisdn", equalTo(validMsisdn));
    }

    // PUT - попытка изменения с невалидным номером
    @Test
    public void changeTariffForSubscriberByManagerWithInvalidNumberTest() {
        String invalidMsisdn = "777";
        int newTariffId = 12;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", invalidMsisdn)
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
        String validMsisdn = "79241263770";
        int newTariffId = 11;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AuthSubscriber.getSubscriberToken())
                .pathParam("subscriberId", validMsisdn)
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
        String nonExistentMsisdn = "77762230331";
        int newTariffId = 11;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", nonExistentMsisdn)
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
        String validMsisdn = "79241263770";
        int nonExistentTariffId = 999;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validMsisdn)
                .pathParam("tariffId", nonExistentTariffId)
                .when()
                .put("/subscribers/{subscriberId}/tariff/{tariffId}")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"));
    }
}