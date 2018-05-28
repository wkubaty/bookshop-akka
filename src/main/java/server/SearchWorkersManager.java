package server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import common.Request;
import common.SearchResponse;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;

import static akka.actor.SupervisorStrategy.restart;

public class SearchWorkersManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private LinkedList<ActorRef> availableActors = new LinkedList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    ActorRef actor = availableActors.pollFirst();
                    if(actor!=null){
                        actor.tell(request, getSelf());
                    }
                })
                .match(SearchResponse.class, response -> {
                    context().parent().tell(response, getSelf());
                    availableActors.add(sender());
                })
                .match(ReceiveTimeout.class, timeout ->{
                    context().parent().tell(timeout, getSelf());
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        for(int i = 0; i < 10; i++){
            ActorRef actor = context().actorOf(Props.create(SearchWorker.class), "searchWorker" + i);
            availableActors.add(actor);
        }
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
