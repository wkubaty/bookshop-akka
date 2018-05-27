package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.OrderResponse;
import common.Request;
import common.StreamResponse;

public class StreamWorker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    getSender().tell(new StreamResponse("some text"), getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
