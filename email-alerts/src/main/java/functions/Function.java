 package functions;

import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.runtime.configuration.ProfileManager;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.io.IOException;

import com.sendgrid.*;

public class Function {
    private static final Logger Log = Logger.getLogger(Function.class);

    @ConfigProperty(name = "sg.from")
    public String EMAIL_FROM;

    @ConfigProperty(name = "sg.to")
    public String EMAIL_TO;

    @ConfigProperty(name = "sg.key")
    public String SENDGRID_API_KEY;

    @Funq
    public Output function(CloudEvent<EventContainer> input) {
        String attacker = input.data().getBonus().getAttacker();
        Integer shots = input.data().getBonus().getShots();

        if (attacker == null || shots == null) {
            throw new NullPointerException("attacker and/or shots values were null");
        }

        try {
            if (ProfileManager.getActiveProfile() != "test") {
                // Lazy way to avoid mocking/testing this...
                Email from = new Email(EMAIL_FROM);
                Email to = new Email(EMAIL_TO);
                String subject = "Audit Failure: Bonus Round";
                Content content = new Content(
                    "text/plain",
                    "The player with ID " + attacker + " might be cheating. They scored " + shots + " shots."
                );
                Mail mail = new Mail(from, subject, to, content);

                SendGrid sg = new SendGrid(SENDGRID_API_KEY);
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                Integer sc = response.getStatusCode();
                
                if (sc >= 200 && sc <= 299) {
                    Log.info("audit email sent for user " + attacker + " with " + shots + " shots");
                } else {
                    throw new Error("SendGrid returned " + sc + "status");
                }
            }

            return new Output("audit email sent for user " + attacker);
        } catch (IOException ex) {
            Log.error("failed to send send audit email for " + attacker);
            Log.error(ex);
            return new Output("error sending audit email for player ID " + attacker);
        }
    }
}
