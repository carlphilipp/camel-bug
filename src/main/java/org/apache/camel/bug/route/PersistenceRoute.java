package org.apache.camel.bug.route;

import com.mongodb.client.model.Filters;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.ExpressionAdapter;
import org.springframework.stereotype.Component;

import static org.apache.camel.bug.route.RestApiRoute.HTTP_HEADER_API_KEY;
import static org.apache.camel.component.mongodb3.MongoDbConstants.CRITERIA;
import static org.apache.camel.component.mongodb3.MongoDbConstants.RESULT_TOTAL_SIZE;

@Component
public class PersistenceRoute extends RouteBuilder {

    @Override
    public void configure() {
        // @formatter:off
        from("direct:persistence-find-key-should-work")
            .setHeader(CRITERIA, apiKeyFilters())
            .to("mongodb3:cosmosdb?database={{db.database}}&collection={{db.clientKeysCollection}}&operation=findOneByQuery")
            .removeHeader(CRITERIA);

        from("direct:persistence-find-key-fixed")
            .setHeader(CRITERIA, apiKeyFilters())
            .to("mongodb3:cosmosdb?database={{db.database}}&collection={{db.clientKeysCollection}}&operation=findOneByQuery")
            .choice()
                .when(header(RESULT_TOTAL_SIZE).isLessThan(1))
                    .setBody(simple("{}")) // FIX to force the body to be not null. It fixes the issue
            .end()
            .removeHeader(CRITERIA);
        // @formatter:on
    }

    private static Expression apiKeyFilters() {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                return Filters.eq("key", exchange.getIn().getHeader(HTTP_HEADER_API_KEY, String.class));
            }
        };
    }
}
