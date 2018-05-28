package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.OrderResponse;
import common.Request;

import java.io.*;

public class OrderWorker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private static final String ordersPath = "database/orders.txt";
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    boolean confirmation = orderBookWithConfirmation(request.getTitle());
                    getSender().tell(new OrderResponse(request.getTitle(), confirmation), getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private boolean orderBookWithConfirmation(String title) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(ordersPath, true), "utf-8"))) {
            writer.write(title);
            return true;
        } catch (IOException ex) {
            log.error("Couldn't order '" + title + "' book");
            return false;
        }
    }

}
