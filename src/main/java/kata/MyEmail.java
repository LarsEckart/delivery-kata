package kata;

public record MyEmail(String contactEmail, String subject, String message) {

    @Override
    public String toString() {
        return String.format("to:%s\nsubject:%s\nmessage:%s\n", contactEmail, subject, message);
    }
}
