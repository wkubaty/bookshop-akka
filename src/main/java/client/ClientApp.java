package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class ClientApp {
    public static void main(String[] args) throws Exception {
        LinkedList<Integer> l = new LinkedList<>();

        File configFile = new File("remote_app.conf");
        Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("client", config);
        final ActorRef local = system.actorOf(Props.create(ClientActor.class), "local");
        System.out.println("Enter your request: [search/order/stream 'book-title']");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            local.tell(line, null);
        }

        system.terminate();
    }
}
