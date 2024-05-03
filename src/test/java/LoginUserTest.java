import clients.UserClient;
import dataprovider.UserProvider;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import request.CreateUserRequest;
import request.LoginUserRequest;
import java.util.Objects;

public class LoginUserTest {
    private UserClient userClient = new UserClient();
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    @Step("Авторизоваться под существующим пользователем")
    @DisplayName("Успешная авторизация под существующим пользователем")
    @Description("Статус кода: 200, В теле ответа есть success (true), email, name, accessToken, refreshToken")
    public void existUserShouldBeAuth() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserRequest();
        //создание
        userClient.create(createUserRequest);
        //авторизация
        LoginUserRequest loginUserRequest = LoginUserRequest.from(createUserRequest);
        accessToken = userClient.login(loginUserRequest)
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
    @Step("Авторизоваться с несуществующим логином")
    @DisplayName("Пользователь не проходит авторизацию, если введен неверный логин")
    @Description("Статус кода: 401, В теле ответа success false и сообщение об ошибке")
    public void unsuccessfulAuthWithIncorrectEmail() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserRequest();
        //создание
        accessToken = userClient.create(createUserRequest)
                .extract().jsonPath().get("accessToken");
        //авторизация с неверным логином
        LoginUserRequest loginUserRequest = LoginUserRequest.from(createUserRequest);
        loginUserRequest.setEmail(RandomStringUtils.randomAlphabetic(8));

        userClient.login(loginUserRequest)
                .statusCode(401)
                .body("success", Matchers.equalTo(false))
                .and()
                .body("message", Matchers.equalTo("email or password are incorrect"));
    }
    @Test
    @Step("Авторизоваться под пользователем с неверным паролем")
    @DisplayName("Пользователь не проходит авторизацию, если введен неверный пароль")
    @Description("Статус кода: 401, В теле ответа success false и сообщение об ошибке")
    public void unsuccessfulAuthWithIncorrectPassword() {
        CreateUserRequest createUserRequest = UserProvider.getRandomCreateUserRequest();
        //создание
        accessToken = userClient.create(createUserRequest)
                .extract().jsonPath().get("accessToken");
        //авторизация с неверным паролем
        LoginUserRequest loginUserRequest = LoginUserRequest.from(createUserRequest);
        loginUserRequest.setPassword(RandomStringUtils.randomAlphabetic(8));

        userClient.login(loginUserRequest)
                .statusCode(401)
                .body("success", Matchers.equalTo(false))
                .and()
                .body("message", Matchers.equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if( !(Objects.equals(accessToken, null)) ) {
            userClient.delete(accessToken)
                    .statusCode(202);
        }
    }
}