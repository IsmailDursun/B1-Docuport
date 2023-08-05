package app.docuport.step_definitions;

import app.docuport.utilities.DocuportApiUtil;
import app.docuport.utilities.Environment;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
public class DocuportStepDefs {

    public static final Logger LOG = LogManager.getLogger();
    String baseUrl = Environment.BASE_URL;
    String accessToken;
    Response response;

    @Given("User logged in to Docuport api as advisor role")
    public void user_logged_in_to_Docuport_api_as_advisor_role() {

        String email = Environment.ADVISOR_EMAIL;
        String password = Environment.ADVISOR_PASSWORD;
        LOG.info("Authorizing adviser user - email: " + email + " - password: " + password);
        LOG.info("Environment base url: " + baseUrl);

        accessToken = DocuportApiUtil.getAccessToken(email, password);

        if (accessToken.isEmpty()) {
            LOG.error("Could not authorize user in authorization server");
            fail("Could not authorize user in authorization server");// Assert.fail() -- this is from JUnit
        } else {
            LOG.info("Access token: " + accessToken);
        }
    }

    @Given("User sends GET request to {string} with query param {string} email address")
    public void user_sends_GET_request_to_with_query_param_email_address(String endpoint, String userType) {
        String email = "";
        switch (userType) {
            case "advisor":
                email = Environment.ADVISOR_EMAIL;
                break;
            case "client":
                email = Environment.CLIENT_EMAIL;
                break;
            case "employee":
                email = Environment.EMPLOYEE_EMAIL;
                break;
            case "supervisor":
                email = Environment.SUPERVISOR_EMAIL;
                break;
            default:
                LOG.info("Invalid user type");
        }

        response = given().accept(ContentType.JSON).and().header("Authorization", accessToken).and().queryParam("EmailAddress", email).when().get(baseUrl + endpoint);

        response.then().log().all();

    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expStatusCode) {
        assertEquals("Invalid Status Code", expStatusCode, response.statusCode()); // Here we can have a message
        response.then().statusCode(expStatusCode); // This is doing the same thing as above with no Message
    }

    @Then("content type is {string}")
    public void content_type_is(String expContentType) {
        response.then().contentType(ContentType.JSON);
        assertEquals("Content type not matching: Expected was: " + expContentType + " but actual was " + response.contentType(), expContentType, response.contentType());
    }

    @Then("role is {string}")
    public void role_is(String expRole) {
        assertEquals(expRole, response.path("items[0].roles[0].name"));

        // This will do the same thing. We are just doing as practive
        JsonPath jsonPath = response.jsonPath();
        assertEquals(expRole, jsonPath.getString("items[0].roles[0].name"));

        // De-Serialization

    }
}