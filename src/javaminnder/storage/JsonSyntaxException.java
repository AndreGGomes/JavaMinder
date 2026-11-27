package javaminnder.storage;

public class JsonSyntaxException extends RuntimeException {

    /*@ public normal_behavior
      @   assignable \nothing;
      @*/
    public JsonSyntaxException(String message) {
        super(message);
    }

    /*@ public normal_behavior
      @   assignable \nothing;
      @*/
    public JsonSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}