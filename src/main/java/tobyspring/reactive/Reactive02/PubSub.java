package tobyspring.reactive.Reactive02;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Reactive Stereams - Operators
 * Publisher -> [Data1] -> Operator -> [Data2] -> Op2 -> [Data3] -> Subscriber
 * <p>
 * 1. map (d1 -> f -> d2)
 * Flow :
 * pub -> [Data1] -> mapPub-> [Data2] -> logSub // DownStream
 */
public class PubSub {
    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<Integer> mapPub = mapPub(pub, i -> i * 10);
//        Publisher<Integer> map2Pub = mapPub(mapPub, i -> -i);
//        Publisher<Integer> sumPub = sumPub(pub);
        Publisher<Integer> reducePub = reducePub(pub, 1, (a, b) -> a * b);
        // 실제 구독
        reducePub.subscribe(logSub());
    }

    private static Publisher<Integer> reducePub(Publisher<Integer> pub, Integer initVal, BiFunction<Integer, Integer, Integer> bf) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {
                    int result = initVal;

                    @Override
                    public void onNext(Integer i) {
                        result = bf.apply(result, i);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return (sub) -> {
            pub.subscribe(new DelegateSub(sub) {
                int sum = 0;

                @Override
                public void onNext(Integer i) {
                    sum += i;
                }

                @Override
                public void onComplete() {
                    sub.onNext(sum);
                    sub.onComplete();
                }
            });
        };
//            @Override
//            public void subscribe(Subscriber<? super Integer> sub) {
//                pub.subscribe(new DelegateSub(sub)  {
//                    int sum = 0;
//                    @Override
//                    public void onNext(Integer i)   {
//                        sum += i;
//                    }
//                    @Override
//                    public void onComplete()    {
//                        sub.onNext(sum);
//                        sub.onComplete();
//                    }
//                });
//            }
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {
                    @Override
                    public void onNext(Integer i) {
                        sub.onNext(f.apply(i));
                    }
                });
            }
        };
    }

    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer t) {
                System.out.println("onNext:" + t);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError:" + t);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return new Publisher<Integer>() {


            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(s -> sub.onNext(s));
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {
                    }

                });
            }
        };
    }

}
