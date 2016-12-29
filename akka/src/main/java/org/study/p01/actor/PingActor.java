package org.study.p01.actor;

import akka.actor.*;
import akka.event.*;

public class PingActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef pong;

    @Override
    public void preStart() throws Exception {
        this.pong = context().actorOf(Props.create(PongActor.class, getSelf()), "pongActor");

    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof String) {
            String msg = (String) message;
            log.info("Ping received {}", msg);
            pong.tell("ping", getSelf());
        }
    }
}
