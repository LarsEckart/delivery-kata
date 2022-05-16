package kata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.spun.util.JsonUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.approvaltests.Approvals;
import org.approvaltests.StoryBoard;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void testActualStuff() {
        Delivery[] delivery = new Delivery[1];

        LocalDateTime llewellynArrived = LocalDateTime.of(2000, 1, 2, 3, 24, 5);
        LocalDateTime llewellynOrdered = LocalDateTime.of(2000, 1, 2, 3, 4, 15);
        LocalDateTime brianOrdered = LocalDateTime.of(2000, 1, 2, 3, 0, 0);

        DeliveryEvent deliveryEvent = new DeliveryEvent(1, llewellynArrived, 58.377066f, 26.727897f);
        Delivery delivery0 = new Delivery(13L, "brian@example.com", 58.377f, 26.727f, brianOrdered, false, false);
        Delivery delivery1 = new Delivery(1L, "llewellyn@example.com", 58.377f, 26.727f, llewellynOrdered, false, false);
        List<Delivery> deliveries = List.of(delivery0, delivery1, delivery0);

        StoryBoard storyBoard = new StoryBoard();

        DeliveryService.onDelivery((t, s, m) -> storyBoard.addFrame("send email", String.format("to:%s\nsubject:%s\nmessage:%s\n", t, s, m)),
                new MapService(), deliveryEvent, d -> delivery[0] = d, deliveries);

        storyBoard.addFrame("saved", JsonUtils.asJson(delivery[0], b -> b.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())));
        Approvals.verify(storyBoard);
    }

    @Test
    void testEdges() {
        Delivery[] delivery = new Delivery[1];

        LocalDateTime llewellynArrived = LocalDateTime.of(2000, 1, 2, 3, 13, 5);
        LocalDateTime llewellynOrdered = LocalDateTime.of(2000, 1, 2, 3, 4, 15);
        LocalDateTime brianOrdered = LocalDateTime.of(2000, 1, 2, 3, 0, 0);

        DeliveryEvent deliveryEvent = new DeliveryEvent(1, llewellynArrived, 58.377066f, 26.727897f);
        Delivery delivery0 = new Delivery(13L, "brian@example.com", 58.377f, 26.727f, brianOrdered, false, false);
        Delivery delivery1 = new Delivery(1L, "llewellyn@example.com", 58.377f, 26.727f, llewellynOrdered, false, false);
        List<Delivery> deliveries = List.of(delivery0, delivery1);

        StoryBoard storyBoard = new StoryBoard();

        DeliveryService.onDelivery((t, s, m) -> storyBoard.addFrame("send email", String.format("to:%s\nsubject:%s\nmessage:%s\n", t, s, m)),
                new MapService(), deliveryEvent, d -> delivery[0] = d, deliveries);

        storyBoard.addFrame("saved", JsonUtils.asJson(delivery[0], b -> b.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())));
        Approvals.verify(storyBoard);
    }

    public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime instant) throws IOException {
            jsonWriter.value(instant.toString());
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            throw new IOException("Never called");
        }
    }


}
