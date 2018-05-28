package server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import common.Request;
import common.SearchResponse;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.restart;

public class SearchWorker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int searchFailCounter;
    private Boolean found;


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    found = false;
                    context().child("databaseSearcher1").get().tell(request, getSelf());
                    context().child("databaseSearcher2").get().tell(request, getSelf());
                    getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
                })
                .match(SearchResponse.class, response -> {
                    getContext().setReceiveTimeout(Duration.Undefined());
                    if(found){
                        return;
                    }
                    if(response.getPrice()==null){
                        if(searchFailCounter == 0){ //no book in one of dbs
                            searchFailCounter++;
                        } else{ //no book in both db
                            searchFailCounter = 0;
                            context().parent().tell(response, getSelf());
                        }
                    } else {
                        searchFailCounter = 0;
                        found = true;
                        context().parent().tell(response, getSelf());
                    }
                })
                .match(ReceiveTimeout.class, timeout -> {
                    getContext().setReceiveTimeout(Duration.Undefined());
                    context().parent().tell(timeout, getSelf());
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(DatabaseSearcher.class, 1), "databaseSearcher1");
        context().actorOf(Props.create(DatabaseSearcher.class, 2), "databaseSearcher2");
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
