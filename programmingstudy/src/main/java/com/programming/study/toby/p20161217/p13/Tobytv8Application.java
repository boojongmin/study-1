package com.programming.study.toby.p20161217.p13;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.concurrent.*;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

import lombok.extern.slf4j.*;

@SpringBootApplication
@Slf4j
@EnableAsync
public class Tobytv8Application {

    @RestController
    public static class  MyController {
        @Autowired MyService myService;

        // TODO: 2016. 12. 17.
        // 제대로 동작 안하는 듯 수정!!!
        // 1시간 40분쯤

        @GetMapping("/async")
        public Callable<String> async() throws InterruptedException {
            log.info("callable");
            return () -> {
                Thread.sleep(2000);
                 return "hello";
            };
        }
    }

    @Component
    public static class MyService {
        public ListenableFuture<String> hello() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
            return new AsyncResult<>("hello");
        }
    }

    public static void main(String[] args) {
        log.debug("hello()");
        SpringApplication.run(Tobytv8Application.class, args);
    }

}
