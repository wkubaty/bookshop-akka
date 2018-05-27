package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.Request;
import common.SearchResponse;

import java.math.BigDecimal;

public class SearchWorker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    sender().tell(new SearchResponse(request.getTitle(), BigDecimal.TEN), getSelf());
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
