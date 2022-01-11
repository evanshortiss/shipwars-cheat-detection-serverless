package functions;

import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@QuarkusTest
public class FunctionTest {
    final static String json = "{\"match\":\"qnSwtjPzSO6tf4kNDmn3V\",\"game\":\"w38nSZQmG8ZlroWU6tiD-\",\"by\":{\"username\":\"Charm Snagglefoot\",\"uuid\":\"ntySV2RY5VNIOphGGWdnZ\"},\"shots\":300,\"human\":false}";

    @Test
    void testFunction() throws JsonMappingException, JsonProcessingException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        Shot shot = mapper.readValue(json, Shot.class);
        CloudEvent<Response> r = (new Function()).function(shot);

        Assertions.assertEquals(r.type(), Function.AUDIT_FAIL + "." + AuditType.Bonus);
    }

    @Test
    public void testFunctionIntegration() {
        RestAssured.given()
                .contentType("application/json")
                .body(json)
                .header("ce-id", "42")
                .header("ce-specversion", "1.0")
                .header("ce-type", "audit.fail")
                .post("/")
                .then().statusCode(200)
                .log().body()
                .body("type", equalTo("Bonus"))
                .body("userId", equalTo("ntySV2RY5VNIOphGGWdnZ"))
                .body("data.shots", equalTo(300));
    }

}
