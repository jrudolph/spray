package spray.examples;

public interface RequestContext {
  <T> T get(Extraction<T> extraction);
  <T> void complete(T value, Marshaller<T> marshaller);
  void complete(String value);
}
