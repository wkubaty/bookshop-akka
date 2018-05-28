package server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import common.Request;
import common.StreamResponse;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;

import static akka.actor.SupervisorStrategy.restart;

public class StreamWorkersManager extends AbstractActor{
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private LinkedList<ActorRef> availableActors = new LinkedList<>();

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    ActorRef actor = availableActors.pollFirst();
                    if(actor!=null){
                        actor.tell(request, getSelf());
                    }
                })
                .match(StreamResponse.class, response -> {
                    context().parent().tell(response, getSelf());
                    if(response.getLine()==null || "EOF".equals(response.getLine())){
                        availableActors.add(sender());
                    }
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

    @Override
    public void preStart(){
        for(int i = 0; i < 10; i++){
            ActorRef actor = context().actorOf(Props.create(StreamWorker.class), "streamWorker" + i);
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
