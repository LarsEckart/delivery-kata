package kata;

public interface EmailGateway {
    void send(String recipient, String subject, String message);
}
