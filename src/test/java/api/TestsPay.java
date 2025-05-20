package api;

import api.additionally.AuthSubscriber;
import api.additionally.ManagerTestBase;
import api.additionally.SubscriberData;
import api.additionally.TestData;
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
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        double paymentAmount = 120.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .body(createRequestBody(paymentAmount))
                .when()
                .patch("/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(200);
    }

    // PATCH - пополнение баланса абонентом (должно быть запрещено)
    @Test
    public void payForSubscriberBySelfTest() {
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        double paymentAmount = 120.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AuthSubscriber.getSubscriberToken())
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
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
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        double negativeAmount = -10.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
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
        SubscriberData validSubscriber = TestData.existValidSubscriber();
        double zeroAmount = 0.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
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
        SubscriberData invalidSubscriber = TestData.nonExistSubscriber();
        double paymentAmount = 100.0;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", invalidSubscriber.getSubscriberId())
                .body(createRequestBody(paymentAmount))
                .when()
                .patch("/manager/subscribers/{subscriberId}/balance")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"));
    }
}