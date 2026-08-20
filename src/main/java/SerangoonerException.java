/** Represents invalid input entered as a Serangooner command. */
public class SerangoonerException extends RuntimeException {
    public SerangoonerException(String message) {
        super(message);
    }

    public SerangoonerException(String message, Throwable cause) {
        super(message, cause);
    }
}
