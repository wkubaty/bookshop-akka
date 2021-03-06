package client;

import akka.actor.AbstractActor;
import akka.actor.ReceiveTimeout;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import common.*;

import java.math.BigDecimal;
import java.util.HashMap;


public class ClientActor extends AbstractActor{
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private String path = "akka.tcp://bookshop@127.0.0.1:3552/user/remote";

    private final static HashMap<String, RequestType> requestMap ;
    static {
        requestMap = new HashMap<>();
        requestMap.put("search", RequestType.SEARCH);
        requestMap.put("order", RequestType.ORDER);
        requestMap.put("stream", RequestType.STREAM);
    }
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, command -> {
                    RequestType requestType = requestMap.get(command.split(" ")[0]);
                    if(requestType!=null){
                        String title = command.substring(command.indexOf(" ")+1);
                        getContext().actorSelection(path).tell(new Request(requestType, title), getSelf());
                    }
                    else{
                        System.out.println("Wrong command! Try again...");
                    }
                })
                .match(SearchResponse.class, searchResponse -> {
                    String title = searchResponse.getTitle();
                    BigDecimal price = searchResponse.getPrice();
                    if(price!=null){
                        System.out.println("'" + title + "' costs: " + price);
                    }
                    else{
                        System.out.println("There is no: '" + title + "' in the bookshop!");
                    }
                })
                .match(OrderResponse.class, orderResponse -> {
                    String title = orderResponse.getTitle();
                    Boolean confirmed = orderResponse.isOrderConfirmed();
                    if(confirmed){
                        System.out.println("Placed an order for: '" + title + "' successfully!");
                    }
                    else{
                        System.out.println("Order failed. There is no: '" + title + "' in the bookshop!");
                    }
                })
                .match(StreamResponse.class, streamResponse -> {
                    String line = streamResponse.getLine();
                    if(line!=null){
                        if(line.equals("EOF")){
                            System.out.println("--END OF THE BOOK--");
                        } else {
                            System.out.println(line);
                        }
                    }
                    else{
                        System.out.println("Couldn't stream. There is no such book in the bookshop!");
                    }
                })
                .match(ReceiveTimeout.class, timeout ->{
                    System.out.println("It may take a while...");
                })
                .matchAny(o -> log.info("Received unknown message"))
                .build();
    }
}
