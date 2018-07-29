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
//        Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]");
//        Publisher<Integer> map2Pub = mapPub(mapPub, i -> -i);
//        Publisher<Integer> sumPub = sumPub(pub);
        Publisher<String> reducePub = reducePub(pub, "", (a, b) -> a + "-" + b);
        // 실제 구독
        reducePub.subscribe(logSub());
    }

    //
    private static Publisher<String> reducePub(Publisher<Integer> pub, String initVal, BiFunction<String, Integer, String> bf) {
        return (sub) -> {
            pub.subscribe(new DelegateSub<Integer, String>(sub) {
                String result = initVal;

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
        };
    }

//    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
//        return (sub) -> {
//            pub.subscribe(new DelegateSub(sub) {
//                int sum = 0;
//
//                @Override
//                public void onNext(Integer i) {
//                    sum += i;
//                }
//
//                @Override
//                public void onComplete() {
//                    sub.onNext(sum);
//                    sub.onComplete();
//                }
//            });
//        };
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
//    }

    // T -> R
    private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return (sub) -> {
            pub.subscribe(new DelegateSub<T, R>(sub) {
                @Override
                public void onNext(T i) {
                    sub.onNext(f.apply(i));
                }
            });
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {

            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T t) {
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
