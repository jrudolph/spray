package spray.examples;

public interface Extraction<T> {
    T get(RequestContext ctx);
}
