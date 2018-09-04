package tobyspring.reactive.Reactive04;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@SpringBootApplication
@Slf4j
@EnableAsync
public class Application {

    @RestController
    public static class MyController {
        @GetMapping("/callable")
        public Callable<String> callable() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("Hello");
                Thread.sleep(2000);
                return "Hello";
            };
        }
//        public String callable() throws InterruptedException {
//            log.info("callable");
//            Thread.sleep(2000);
//            return "Hello";
//        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
