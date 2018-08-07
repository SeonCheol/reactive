package tobyspring.reactive.Reactive01;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author seoncheol
 */
public class PubSub {
	public static void main(String[] args) throws InterruptedException {
		// Publisher  <- Observable
		/*
		 * A Publisher is a provider of a potentially unbounded number of sequenced elements, publishing them according to the demand received from its Subscriber(s).
		 */
		
		/**
		 * 1. Publisher & Subscriber 기본 구현
		 */
		//    	Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
		//    	
		//    	Publisher p = new Publisher() {
		//    		@Override
		//    		public void subscribe(Subscriber subscriber)	{
		//    			subscriber.onSubscribe(new Subscription() {
		//					
		//					@Override
		//					public void request(long n) {
		//					}
		//					
		//					@Override
		//					public void cancel() {
		//					}
		//				});
		//    		}
		//		};
		//		Subscriber s = new Subscriber() {
		//
		//			@Override
		//			public void onSubscribe(Subscription s) {
		//			}
		//
		//			@Override
		//			public void onNext(Object t) {
		//			}
		//
		//			@Override
		//			public void onError(Throwable t) {
		//			}
		//
		//			@Override
		//			public void onComplete() {
		//			}
		//		};
		//    	
		//    	p.subscribe(s);

		/**
		 * 2. 실제 데이터 넘기는 구현
		 */
//		Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
//
//		Publisher p = new Publisher() {
//			@Override
//			public void subscribe(Subscriber subscriber) {
//				subscriber.onSubscribe(new Subscription() {
//					Iterator<Integer> it = itr.iterator();
//
//					@Override
//					public void request(long n) {
//						while (n-- > 0) {
//							if (it.hasNext()) {
//								subscriber.onNext(it.next());
//							} else {
//								subscriber.onComplete();
//								break;
//							}
//						}
//					}
//
//					@Override
//					public void cancel() {
//					}
//				});
//			}
//		};
//		Subscriber s = new Subscriber() {
//			// Subscription 저장
//			Subscription subscription;
//			@Override
//			public void onSubscribe(Subscription s) {
//				System.out.println("onSubscribe");
//				// s.request(Long.MAX_VALUE);
//				this.subscription = s;
//				this.subscription.request(1);
//			}
//
//			@Override
//			public void onNext(Object t) {
//				System.out.println("onNext" + t);
//				 this.subscription.request(1); //  
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				System.out.println("onError");
//			}
//
//			@Override
//			public void onComplete() {
//				System.out.println("onComplete");
//			}
//		};
//
//		p.subscribe(s);
		
		/**
		 * 동기화 구현
		 */

		        Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
//		        ExecutorService es = Executors.newSingleThreadExecutor(); // for 병렬처리
		ExecutorService es = Executors.newFixedThreadPool(2);
		        Publisher p = new Publisher() {
		            Iterator<Integer> it = itr.iterator();
		
		            @Override
		            public void subscribe(Subscriber subscriber) {
		                subscriber.onSubscribe(new Subscription() {
		                    //
		                    @Override
		                    public void request(long n) {
		
		                        es.submit(() -> {
		//                        Future<?> future = es.submit(() -> {
		                            try {
		                                int i = 0;
		//                                while (n-- > 0) { // 람다 식 밖에서 정의된 변수 사용 안됨.
		                                while (i++ < n) {
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
		                this.subscription.request(3);
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
//		                es.shutdown();
		            }
		        };
		
		        p.subscribe(s);
		        es.awaitTermination(10, TimeUnit.HOURS);
		       es.shutdown();
	}
}
