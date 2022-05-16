package kata;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class SendgridEmailGateway {

    private static final Logger log = getLogger(SendgridEmailGateway.class);

    public void send(String to, String s, String m) {
        Email from = new Email("deliveries@example.com");
        Content content = new Content("text/plain", m);
        Mail mail = new Mail(from, s, new Email(to), content);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

        Response response;
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        try {
            request.setBody(mail.build());
            response = sg.api(request);
        } catch (IOException ex) {
            throw new RuntimeException("something went wrong");
        }
        log.info("{}: {}", response.getStatusCode(), response.getBody());
        if (response.getStatusCode() != 200) {
            throw new RuntimeException(response.getBody());
        }
    }
}
