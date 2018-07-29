package tobyspring.reactive.Reactive02;


import reactor.core.publisher.Flux;

/**
 * @author seoncheol
 */
public class ReactorEx {
    public static void main(String[] args) {
        Flux.<Integer>create(s -> {
            s.next(1);
            s.next(2);
            s.next(3);
            s.next(4);
            s.complete();
        }).log()
                .map(s -> s * 10)
                .reduce(0, (a,b) ->a+b)
                .log()
                .subscribe(System.out::println);
    }
}
