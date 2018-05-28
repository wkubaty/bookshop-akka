package server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import common.*;
import scala.concurrent.duration.Duration;

import java.util.HashMap;

import static akka.actor.SupervisorStrategy.restart;

public class ServerActor extends AbstractActor{
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private String path = "akka.tcp://client@127.0.0.1:2552/user/local";

    private final static HashMap<RequestType, String> requestWorkerMap ;
    static {
        requestWorkerMap = new HashMap<>();
        requestWorkerMap.put(RequestType.SEARCH, "searchWorkersManager");
        requestWorkerMap.put(RequestType.ORDER, "orderWorker");
        requestWorkerMap.put(RequestType.STREAM, "streamWorkersManager");
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    log.info("Received request: " + request.getRequestType());
                    String worker = requestWorkerMap.get(request.getRequestType());
                    if(worker!=null){
                        context().child(worker).get().tell(request, getSelf());
                    } else{
                        log.info("received wrong message");
                    }
                })
                .match(SearchResponse.class, searchResponse -> {
                    log.info("Sending search response to client");
                    getContext().actorSelection(path).tell(searchResponse, getSelf());
                })
                .match(OrderResponse.class, orderResponse -> {
                    log.info("Sending order response to client");
                    getContext().actorSelection(path).tell(orderResponse, getSelf());
                })
                .match(StreamResponse.class, streamResponse -> {
                    log.info("Sending stream response to client");
                    getContext().actorSelection(path).tell(streamResponse, getSelf());
                })
                .match(ReceiveTimeout.class, timeout -> {
                    log.info("Sending timeout to client");
                    getContext().actorSelection(path).tell(timeout, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchWorkersManager.class), "searchWorkersManager");
        context().actorOf(Props.create(OrderWorker.class), "orderWorker");
        context().actorOf(Props.create(StreamWorkersManager.class), "streamWorkersManager");
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder
            .matchAny(o -> restart())
            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
