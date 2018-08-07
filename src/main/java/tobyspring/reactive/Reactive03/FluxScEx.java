package tobyspring.reactive.Reactive03;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxScEx {
    public static void main(String[] args) throws InterruptedException {
        /* 1 */
//        Flux.range(1, 10)
//                .publishOn(Schedulers.newSingle("pub"))
//                .log()
//                .subscribeOn(Schedulers.newSingle("sub"))
//                .subscribe(System.out::println);
//        System.out.println("exit");
        /* 2 */
//        Flux.interval(Duration.ofMillis(500))
//                .subscribe(s -> log.debug("onNext:{}", s));
//        TimeUnit.SECONDS.sleep(5);

        /* 2-1 */
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
            }
            log.debug("Hello");
        });
        log.debug("exit");


    }
}
