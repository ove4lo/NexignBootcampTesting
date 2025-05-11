// 1. Исправленный базовый класс (ManagerTestBase.java)
package api.additionally;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeClass;

public class ManagerTestBase {
    protected static String authToken;
    private static final String BASE_URL = "http://localhost:8080/crm/manager";

    @BeforeSuite
    public void setUpSuite() {
        authToken = AuthManager.getManagerToken();

        if (authToken == null) {
            throw new RuntimeException("Не удалось получить токен авторизации!");
        }
    }

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}