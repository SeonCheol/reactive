package tobyspring.reactive.Reactive02;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class DelegateSub implements Subscriber<Integer> {
    Subscriber sub;

    public DelegateSub(Subscriber sub) {
        this.sub = sub;
    }

    @Override
    public void onSubscribe(Subscription s) {
        sub.onSubscribe(s); // 중개 역할
    }

    @Override
    public void onNext(Integer i) {
        sub.onNext(i);
    }

    @Override
    public void onError(Throwable t) {
        sub.onError(t);
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}
