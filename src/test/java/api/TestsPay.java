package api;

import api.additionally.AuthSubscriber;
import api.additionally.ManagerTestBase;
import io.restassured.http.ContentType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestsPay extends ManagerTestBase {

    // Вспомогательный метод для создания тела запроса
    private Map<String, Object> createRequestBody(double amount) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", amount);
        return requestBody;
    }

    // PATCH - успешное пополнение баланса менеджером
    @Test
    public void payForSubscriberByManagerTest() {
        String validSubscriberId = "79241263770";
        double paymentAmount = 120.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriberId)
                .body(createRequestBody(paymentAmount))
                .when()
                .patch("/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(200);
    }

    // PATCH - пополнение баланса абонентом (должно быть запрещено)
    @Test
    public void payForSubscriberBySelfTest() {
        String subscriberId = "79241263770";
        double paymentAmount = 120.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AuthSubscriber.getSubscriberToken())
                .pathParam("subscriberId", subscriberId)
                .body(createRequestBody(paymentAmount))
                .when()
                .patch("/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(403)
                .body("status", equalTo(403))
                .body("error", equalTo("Forbidden"));
    }

    // PATCH - пополнение отрицательной суммой
    @Test
    public void payWithNegativeSumForSubscriberByManagerTest() {
        String validSubscriberId = "79241263770";
        double negativeAmount = -10.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriberId)
                .body(createRequestBody(negativeAmount))
                .when()
                .patch("/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }

    // PATCH - пополнение нулевой суммой
    @Test
    public void payWithNullForSubscriberByManagerTest() {
        String validSubscriberId = "79241263770";
        double zeroAmount = 0.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriberId)
                .body(createRequestBody(zeroAmount))
                .when()
                .patch("/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }

    // PATCH - пополнение несуществующему абоненту
    @Test
    public void payForNoSubscriberByManagerTest() {
        String nonExistentSubscriberId = "70000000000";
        double paymentAmount = 100.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", nonExistentSubscriberId)
                .body(createRequestBody(paymentAmount))
                .when()
                .patch("/manager/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"));
    }
}