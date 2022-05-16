package kata;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.approvaltests.Approvals;
import org.approvaltests.JsonApprovals;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@MicronautTest
class DeliveryTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void it_works() {
        var postRequest = HttpRequest.POST("/delivery/new", """
                {
                  "email": "test@example.com",
                  "latitude": 58.377065,
                  "longitude": 26.727897
                }"""
        );

        client.toBlocking().exchange(postRequest);
        postRequest = HttpRequest.POST("/delivery", """
                {
                  "id": 2,
                  "timeOfDelivery": "%s",
                  "latitude": 58.377066,
                  "longitude": 26.727897
                }""".formatted(LocalDateTime.now())
        );
        client.toBlocking().exchange(postRequest);
    }
}
