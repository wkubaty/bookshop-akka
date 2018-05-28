package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.Request;
import common.SearchResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Scanner;

public class DatabaseSearcher extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private int dbNumber;

    public DatabaseSearcher(int dbNumber) {
        this.dbNumber = dbNumber;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                    String title = request.getTitle();
                    BigDecimal price = searchInDatabase(title);
                    //Thread.sleep(10000);
                    sender().tell(new SearchResponse(title, price), getSelf());
                })
                .matchAny(o -> log.info("received unknown message: " + o.getClass()))
                .build();
    }

    private BigDecimal searchInDatabase(String title){
        try{
            File file = new File(getDbPath());
            Scanner input = new Scanner(file);

            while(input.hasNextLine()) {
                String line = input.nextLine();
                String[] tokens = line.split(";");
                if(tokens[0].equals(title)){
                    return new BigDecimal(tokens[1]);
                }
            }
        }
        catch (FileNotFoundException e){
            log.error("File: " + title + "not found");
            return null;
        }
        return null;
    }

    private String getDbPath(){
        return "database/db" + dbNumber + ".txt";
    }
}
