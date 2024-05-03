import clients.UserClient;
import dataprovider.UserProvider;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import request.CreateUserRequest;
import java.util.Objects;

public class CreateUserTest {
    private UserClient userClient = new UserClient();
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    @Step("Создать уникального пользователя")
    @DisplayName("Успешное создание уникального пользователя")
    @Description("Статус кода: 200, В теле ответа есть success (true), email, name, accessToken, refreshToken")
    public void uniqueUserShouldBeCreated() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserRequest();
        //создание. из тела берем access token
        accessToken = userClient.create(createUserRequest)
                .statusCode(200)
                .body("success", Matchers.equalTo(true))
                .and()
                .body("user.email", Matchers.equalTo(createUserRequest.getEmail().toLowerCase()))
                .and()
                .body("user.name", Matchers.equalTo(createUserRequest.getName()))
                .and()
                .body("accessToken", Matchers.notNullValue())
                .and()
                .body("refreshToken", Matchers.notNullValue())
                .extract().jsonPath().get("accessToken");
    }

    @Test
    @Step("Создать пользователя, который уже зарегистрирован")
    @DisplayName("Пользователь, который уже зарегистрирован - не будет создан")
    @Description("Статус кода: 403, тело ответа: success false, message 'User already exists'")
    public void sameUserDontBeCreated() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserRequest();
        //создание
        accessToken = userClient.create(createUserRequest)
                .statusCode(200)
                .extract().jsonPath().get("accessToken");
        //повторное создание
        userClient.create(createUserRequest)
                .statusCode(403)
                .body("success", Matchers.equalTo(false))
                .and()
                .body("message", Matchers.equalTo("User already exists"));
    }

    @Test
    @Step("Создать пользователя и не заполнить одно обязательных полей(email)")
    @DisplayName("Пользователь не создается, если не указать email при создании")
    @Description("Статус кода: 403, тело ответа: success false, message \"Email, password and name are required fields\"")
    public void userDontBeCreatedWithoutEmail() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserWithoutEmailRequest();
        //создание
        userClient.create(createUserRequest)
                .statusCode(403)
                .body("success", Matchers.equalTo(false))
                .and()
                .body("message", Matchers.equalTo("Email, password and name are required fields"));
    }

    @Test
    @Step("Создать пользователя и не заполнить одно обязательных полей(пароль)")
    @DisplayName("Пользователь не создается, если не указать password при создании")
    @Description("Статус кода: 403, тело ответа: success false, message \"Email, password and name are required fields\"")
    public void userDontBeCreatedWithoutPassword() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserWithoutPasswordRequest();
        //создание
        userClient.create(createUserRequest)
                .statusCode(403)
                .body("success", Matchers.equalTo(false))
                .and()
                .body("message", Matchers.equalTo("Email, password and name are required fields"));
    }

    @Test
    @Step("Создать пользователя и не заполнить одно обязательных полей(имя)")
    @DisplayName("Пользователь не создается, если не указать name при создании")
    @Description("Статус кода: 403, тело ответа: success false, message \"Email, password and name are required fields\"")
    public void userDontBeCreatedWithoutName() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserWithoutNameRequest();
        //создание
        userClient.create(createUserRequest)
                .statusCode(403)
                .body("success", Matchers.equalTo(false))
                .and()
                .body("message", Matchers.equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if( !(Objects.equals(accessToken, null)) ) {
            userClient.delete(accessToken)
                    .statusCode(202);
        }
    }
}