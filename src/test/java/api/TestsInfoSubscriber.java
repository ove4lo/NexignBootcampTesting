package api;

import api.additionally.ManagerTestBase;
import api.additionally.SubscriberData;
import api.additionally.TestData;
import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestsInfoSubscriber extends ManagerTestBase {

    // GET - успешное получение информации об абоненте
    @Test
    public void getInfoAboutSubscriberTest() {
        SubscriberData validSubscriber = TestData.existValidSubscriber();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .when()
                .log().all()
                .get("/subscribers/{subscriberId}")
                .then()
                .log().all()
                .statusCode(200);
    }

    // GET - невалидный формат идентификатора абонента
    @Test
    public void getInfoAboutSubscriberWithInvalidFormat() {
        SubscriberData invalidSubscriber = TestData.nonExistSubscriber();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", invalidSubscriber.getSubscriberId())
                .when()
                .get("/subscribers/{subscriberId}")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }

    // GET - запрос без аутентификации
    @Test
    public void getInfoAboutSubscriberUnauthorized() {
        SubscriberData validSubscriber = TestData.existValidSubscriber();

        given()
                .contentType(ContentType.JSON)
                .pathParam("subscriberId", validSubscriber.getSubscriberId())
                .when()
                .get("/subscribers/{subscriberId}")
                .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("error", equalTo("Unauthorized"));
    }

    // GET - запрос для несуществующего абонента
    @Test
    public void getInfoAboutNonExistentSubscriber() {
        String nonExistentSubscriberId = "70000000000";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("subscriberId", nonExistentSubscriberId)
                .when()
                .get("/subscribers/{subscriberId}")
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }
}