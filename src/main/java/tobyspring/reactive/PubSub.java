package tobyspring.reactive;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.*;
import java.util.concurrent.Future;

/**
 * @author seoncheol
 */
public class PubSub {
    public static void main(String[] args) {
        // Publisher  <- Observable
        /*
         * A Publisher is a provider of a potentially unbounded number of sequenced elements, publishing them according to the demand received from its Subscriber(s).
         */

        Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService es = Executors.newSingleThreadExecutor(); // for 병렬처리

        Publisher p = new Publisher() {
            Iterator<Integer> it = itr.iterator();

            @Override
            public void subscribe(Subscriber subscriber) {
                subscriber.onSubscribe(new Subscription() {
                    //
                    @Override
                    public void request(long n) {

                        es.execute(() -> {
//                        Future<?> future = es.submit(() -> {
                            try {
                                int i = 0;
//                                while (n-- > 0) {
                                while(i++ < n)  {
                                    if (it.hasNext()) {
                                        subscriber.onNext(it.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (RuntimeException e) {
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                        System.out.println("cancel");
                    }
                });
            }
        };
        // Subscriber <- Observer
        Subscriber<Integer> s = new Subscriber<Integer>() {
            Subscription subscription;

            // 필수
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("onSubscribe()");
//                subscription.request(Long.MAX_VALUE);
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + "-onNext " + item);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + "-onComplete()");
                es.shutdown();
            }
        };

        p.subscribe(s);
    }
}
