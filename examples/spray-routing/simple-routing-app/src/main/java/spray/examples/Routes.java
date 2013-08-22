package spray.examples;

public abstract class Routes {
    public static Route get(Route... inner) { return RoutesImpl.get(inner); }
    public static Route path(String pattern, Route... inner) { return RoutesImpl.path(pattern, inner); }
    public static Route handle(Handler handler) { return RoutesImpl.handle(handler); }
    public static Route complete(String staticValue) { return RoutesImpl.complete(staticValue); }
}
