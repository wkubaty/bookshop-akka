package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import common.Request;
import common.StreamResponse;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class StreamWorker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorMaterializer materializer = ActorMaterializer.create(context().system());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    String path = getFilePath(getFilePath(request.getTitle()));
                    File file = new File(path);
                    if(!file.exists() && !file.canRead()){
                        sender().tell(new StreamResponse(null), getSelf());
                    } else {
                        FileIO.fromPath(Paths.get(path))
                                .via(Framing. delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW)
                                        .map(line -> new StreamResponse(line.utf8String())))
                                .throttle(1, Duration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
                                .to(Sink.actorRef(sender(), new StreamResponse("EOF")))
                                .run(materializer);
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private static String getFilePath(String title){
        return "database/" + title + ".txt";
    }
}
