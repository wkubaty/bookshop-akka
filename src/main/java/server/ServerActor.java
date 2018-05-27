package server;

import akka.actor.AbstractActor;
import akka.actor.AllForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import common.Request;
import common.RequestType;
import scala.concurrent.duration.Duration;

import java.util.HashMap;

import static akka.actor.SupervisorStrategy.restart;

public class ServerActor extends AbstractActor{
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final static HashMap<RequestType, String> requestWorkerMap ;
    static {
        requestWorkerMap = new HashMap<>();
        requestWorkerMap.put(RequestType.SEARCH, "searchWorker");
        requestWorkerMap.put(RequestType.ORDER, "orderWorker");
        requestWorkerMap.put(RequestType.STREAM, "streamWorker");
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    log.info("Received: " + request.getRequestType() + " request");
                    String worker = requestWorkerMap.get(request.getRequestType());
                    context().child(worker).get().tell(request, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchWorker.class), "searchWorker");
        context().actorOf(Props.create(OrderWorker.class), "orderWorker");
        context().actorOf(Props.create(StreamWorker.class), "streamWorker");
    }

    private static SupervisorStrategy strategy
            = new AllForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder
            .matchAny(o -> restart())
            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
