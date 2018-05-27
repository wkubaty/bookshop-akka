package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.OrderResponse;
import common.Request;

public class OrderWorker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    getSender().tell(new OrderResponse(request.getTitle(), true), getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
