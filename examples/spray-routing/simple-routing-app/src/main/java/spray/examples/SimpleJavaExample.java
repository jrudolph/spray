package spray.examples;

import static spray.examples.Routes.*;

public class SimpleJavaExample {
    public static void main(String[] args) {
        final Extraction<Integer> amount = Extractions.intParameter("amount");
        Handler queryHandler = new Handler() {
            @Override
            public void handle(RequestContext ctx) {
                int val = amount.get(ctx);
                ctx.complete("The value is "+val+". It's square is "+(val * val));
            }
        };

        SimpleJavaRoutingApp.run("localhost", 8080,
                get(
                        path("", complete("This is the main page!")),
                        path("query", handle(queryHandler))
                )
        );

    }
}
