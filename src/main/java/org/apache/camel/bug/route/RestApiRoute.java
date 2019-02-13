package org.apache.camel.bug.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.component.mongodb3.MongoDbConstants.RESULT_TOTAL_SIZE;

@Component
public class RestApiRoute extends RouteBuilder {

    public static final String HTTP_HEADER_API_KEY = "x-api-key";

    @Override
    public void configure() {
        // @formatter:off
        rest("/endpoint")
            .get()
            .route()
                .to("direct:persistence-find-key-should-work")
                .to("direct:handle-response");

        rest("/endpoint-fixed")
            .get()
            .route()
                .to("direct:persistence-find-key-fixed")
                .to("direct:handle-response");

        from("direct:handle-response")
            .choice()
                .when(header(RESULT_TOTAL_SIZE).isLessThan(1))
                    .setHeader(HTTP_RESPONSE_CODE, constant(HTTP_UNAUTHORIZED))
                .otherwise()
                    .setHeader(HTTP_RESPONSE_CODE, constant(HTTP_OK))
            .end()
            .setBody(simple(null));
        // @formatter:on
    }
}
