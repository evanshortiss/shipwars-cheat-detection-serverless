package functions;

import org.jboss.logging.Logger;
import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventBuilder;

public class Function {
    private static final Logger Log = Logger.getLogger(Function.class);

    public static final String AUDIT_PASS = "audit.pass";
    public static final String AUDIT_FAIL = "audit.fail";
    public static final Integer CHEAT_THRESHOLD = getCheatThreshold();


    @Funq
    public <T extends BonusMetadata> CloudEvent<Response<T>> function(Shot input) throws InterruptedException {
        // Simulate some processing
        Thread.sleep(500);

        Response<BonusMetadata> r = new Response<BonusMetadata>(
            AuditType.Bonus,
            input.getBy().getUuid(),
            new BonusMetadata(input.getShots())
        );

        if (input.getShots() >= CHEAT_THRESHOLD) {
            Log.infov("User {0} scored {1} points. They might be cheating!", input.getBy().getUsername(), input.getShots());

            return buildResponse(AUDIT_FAIL, r);
        } else {
            Log.infov("User {0} scored {1} points. They're probably not cheating.", input.getBy().getUsername(), input.getShots());
            
            return buildResponse(AUDIT_PASS, r);
        }
    }

    private CloudEvent<Response<T>> buildResponse(String result, Response<T> r) {
        return CloudEventBuilder.create()
            .type(result + "." + AuditType.Bonus)
            .build(r);
    }

    /**
     * Cheat threshold can be defined using the CHEAT_THRESHOLD environment variable.
     * If CHEAT_THRESHOLD is not set, then the default value defined in this function is used.
     * @return
     */
    private static Integer getCheatThreshold () {
        Integer defaultThreshold = 100;
        String envThreshold = System.getenv("CHEAT_THRESHOLD");

        if (envThreshold != null) {
            try {
               Integer parsedThreshold = Integer.parseInt(System.getenv("CHEAT_THRESHOLD"));

               Log.infov("Using CHEAT_THRESHOLD value of {1} from environment", parsedThreshold);

               return parsedThreshold;
            } catch (NumberFormatException ex) {
                Log.errorv("Failed to parse CHEAT_THRESHOLD value \"{1}\" using default value of {2}", System.getenv("CHEAT_THRESHOLD"), defaultThreshold);

                return defaultThreshold;
            }
        } else {
            Log.infov("No CHEAT_THRESHOLD set in environment. Using default value {1}", defaultThreshold);

            return defaultThreshold;
        }
    }

}
