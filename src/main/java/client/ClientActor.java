package client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.Request;
import common.RequestType;
import common.StreamResponse;


public class ClientActor extends AbstractActor{
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private String path = "akka.tcp://remote_system@127.0.0.1:3552/user/remote";
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, request -> {
                    if (request.startsWith("search")) {
                        getContext().actorSelection(path).tell(new Request(RequestType.SEARCH, request), getSelf());

                    }
                    else if (request.startsWith("order")) {
                        getContext().actorSelection(path).tell(request, getSelf());

                    }
                    else if (request.startsWith("stream")) {
                        getContext().actorSelection(path).tell(request, getSelf());

                    }
                    else{
                        System.out.println("Wrong request: " + request);
                    }

                })
                .match(StreamResponse.class, response -> {
                    System.out.println(response);
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
