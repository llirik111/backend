package app.domain.exception;

public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message);
    }
}
