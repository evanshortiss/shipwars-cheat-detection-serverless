package functions;

import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class FunctionTest {

    public static final String jsonbody = "{\"match\":\"qnSwtjPzSO6tf4kNDmn3V\",\"game\":\"w38nSZQmG8ZlroWU6tiD-\",\"by\":{\"username\":\"Charm Snagglefoot\",\"uuid\":\"ntySV2RY5VNIOphGGWdnZ\"},\"shots\":3,\"human\":false}";

    @Test
    void testFunction() {
        Bonus bonus = new Bonus();
        bonus.setAttacker("foobar");
        bonus.setShots(50);
        
        CloudEvent ce = CloudEventBuilder.create().build(bonus);
        Output output = (new Function()).function(ce);
        Assertions.assertEquals("audit email sent for user fubar", output.result);
    }

    @Test
    public void testFunctionIntegration() {
        RestAssured.given().contentType("application/json")
            .body(jsonbody)
            .header("ce-id", "42")
            .header("ce-specversion", "1.0")
            .post("/")
            .then().statusCode(200)
            .body("result", equalTo("audit email sent for user foobar"));
    }

}
