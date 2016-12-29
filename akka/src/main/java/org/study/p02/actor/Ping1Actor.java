package org.study.p02.actor;

import akka.actor.*;
import akka.event.*;

public class Ping1Actor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorRef child1;
    private ActorRef child2;

    public Ping1Actor() {
        child1 = context().actorOf(Props.create(Ping2Actor.class), "ping2Actor");
        child2 = context().actorOf(Props.create(Ping3Actor.class), "ping3Actor");
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            String msg = (String) message;
            if ("work".equals(msg)) {
                log.info("Ping1 received {}", msg);
                child1.tell("work", getSender());
                child2.tell("work", getSender());
            }
        }
    }
}
