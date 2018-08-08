### Reactive 란?

이벤트 방식의 프로그래밍?

Reactive?

Functional Reactive?

> Reactive 외부 이벤트나 데이터 변경 같이 어떠한 사건이 발생하면 그 것에 맞추어 대응하는 프로그래밍 방식.

**Duality** 
리액티브에 대한 아이디어를 얘기할 때 항상 나오는 개념. 수학적 개념으로 구조를 뒤집어서 구성하는 것을 말한다. 한국어로는 ''쌍대성''

**Observer Pattern**
listener와 event를 이용한 event drive 방식의 패턴

**Reactive Streams**
JVM 기술을 개발하는 회사들이 만든 표준 -> Java9 API에 포함.

### Iterable 상속

- Iterable : 내 안의 요소들을 순회 할 수 있어
- Iterator :  Iterable의 요소를 순회할 때 이용(도구)

```java
List<Integer> list = Arrays.asList(1, 2, 3);
for(Integer i : list)	{ // for-each
    System.out.println(i);
}
```

위의 코드는 List 라서 for-each 구문을 사용할 수 있는 게 아니라 List가 Iterable을 구현하고 있기 때문에 for-each 구문을 사용할 수 있다. 

이는 다시 말해, Collection이 아니더라도 Iterable을 상속하면 for-each 를 사용할 수 있다는 것이다.

##### 커스텀 Iterable 구현

```java
public interface Iterable<T> {
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    Iterator<T> iterator(); // 순수하게 구현해야 할 메소드
    ....
```

Iterable 인터페이스에는 forEach(), spliterator(), iterator() 세 가지의 메소드가 있지만 앞의 두 메소드는 default로 구현되어 있어서 Iterable을 사용하기 위해 순수하게 구현해야할 메소드는 iterator() 하나 뿐이다.

```java
Iterable<Integer> iter = () -> new Iterator<Integer> () {
    int i = 0;
    final static int MAX = 10;
    @Override
	public boolean hasNext() {	// 다음 원소 반환 가능한 지 여부
		return i < MAX;
	}
	@Override
	public Integer next() { // 다음 원소 반환 
		return ++i;
	}
};

for(Integer i : iter)	{
    System.out.println(i);
}

// Java 8 이전
for(Iterator<Integer> it = iter.iterator(); it.hasNext(); )	{
    System.out.println(it.next());
}
```

### Iterable과 Observable

Iterable의 쌍대성(duality) : Observable 

- 기능은 동일하나 데이터의 흐름이 반대방향
- Iterable : Pull 개념
  iterator를 이용해서 데이터를 땡겨오는 방식 

- Observable : Push 개념
  특정 데이터나 이벤트를 가지고 있는 소스쪽에서 데이터를 밀어 넣어주는 방식

#### Observable 예제

Observable(source) -> Event/Data -(notify)-> Observer

```java
static class IntegerObservable extends Observable implements Runnable	{
    @Override 
    public void run()	{
        for(int i=1; i<=10; i++)	{
            setChanged();	// 변화 발생
            notifyObservers(i);	// notify push 방식
            // int i = it.next();;	// pull
        }
    }
}


main {
    Observer ob = new Observer(0	{
        
        // 데이터 받는 소스
        @Override
        public void update(Observable o, Object arg)	{
             System.out.println(Thread.currentThread().getName() + "-" + arg);
        }
    });
    
    IntegerObservable io = new IntegerObservable();
    io.addObserver(ob);
    
    // Observer 패턴을 이용하면 별도의 쓰레드를 이용한 구현 손쉬워짐.
    ExcutorService es = Excutors.newSingleThreadExecutor();
    es.execute(io);
    
    System.out.println(Thread.currentThread().getName());
    
    //io.run();
}
```

##### Observer 패턴에 대한 지적

1. Complete에 대한 개념 없음.
2. 익셉션 처리에 대한 구현이 되어 있지 않음.

### Reactive Programming

Observer 패턴에 대한 지적 사항을 해결

Reactive의 큰 두개의 그룹

- ReactiveX  (reactive.io)
- Reactive-Streams (reactive-streams.org)

##### org.reactivestreams

표준 API (github)

- Producer<T, R>

- Publisher<T>

  Publisher.subscribe(Subscriber)를 통해 Subscriber를 등록한다.

  > onSubscribe onNext* (onError | onComplete)

- Subscriber<T>

- Subscription
  Subscriber와 Publisher 사이에 존재하는 객체로 Publisher와 Subscriber 의 속도차이를 컨트롤 하기 위한 기술. 메모리 효율 좋아짐.

Publisher와 Subscriber를 기본 구현.

```java
Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
    	
    	Publisher p = new Publisher() {
    		@Override
    		public void subscribe(Subscriber subscriber)	{
    			subscriber.onSubscribe(new Subscription() {
					
					@Override
					public void request(long n) {
					}
					
					@Override
					public void cancel() {
					}
				});
    		}
		};
		Subscriber s = new Subscriber() {

			@Override
			public void onSubscribe(Subscription s) {
			}

			@Override
			public void onNext(Object t) {
			}

			@Override
			public void onError(Throwable t) {
			}

			@Override
			public void onComplete() {
			}
		};
    	
    	p.subscribe(s);
```

내용 구현

```java
Iterable<Integer> itr = Arrays.asList(1,2,3,4,5);
Publisher p = new Publisher() {
			@Override
			public void subscribe(Subscriber subscriber) {
				subscriber.onSubscribe(new Subscription() {
					Iterator<Integer> it = itr.iterator();

					@Override
					public void request(long n) {
						// while (true) {                        	
                        try	{
                            while(n-- > 0)	{
                                if (it.hasNext()) {
                                    subscriber.onNext(it.next());
                                } else {
                                    subscriber.onComplete();
                                    break;
                                }
                            }
                        }	catch(RuntimeException e)	{
                            subscriber.onError(e);
                        }
					}

					@Override
					public void cancel() {
					}
				});
			}
		};
		Subscriber s = new Subscriber() {

			@Override
			public void onSubscribe(Subscription s) {
				System.out.println("onSubscribe");
				// s.request(Long.MAX_VALUE); // 무제한 요청
                  s.request(1); // 하나씩 
			}

			@Override
			public void onNext(Object item) {
				System.out.println("onNext" + item);
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("onError" + t);
			}

			@Override
			public void onComplete() {
				System.out.println("onComplete");
			}
		};

		p.subscribe(s);

```

onNext에서 요청을 하나 처리하고 다음 요청을 호출하는 코드 추가

```java
//Subscriber...
Subscription subscription; // Subscriber 객체에서 Subscription 저장
@Override
public void onSubscribe(Subscription s)	{
	....
    this.subscription = s;
    this.subscription.request(1);
}

@Override
public void onNext(Integer item)	{
    ...
    this.subscription.request(1);	// 작업 하나 처리하면 다음 작업 호출
}
```

실제 버퍼 사이즈를 고려한 코드 추가

```java
// Subscriber... 
int bufferSize = 2;

@Override
public void onNext(Integer item)	{
    /* 작업 처리.. */
    
    if( --bufferSize <= 0 )	{
        bufferSize = 2;
        this.subscription.request(1);
    }
}
```

엔진을 만드는 코드라 복잡해 보이지만 다 만들어져 있는 코드들!

#### 비동기

Publisher가 데이터를 다수의 쓰레드를 통해 Subscriber에게 넘기는 작업이 가능??

-> 스펙상 불가능 : Subscriber는 데이터가 sequential 하게 데이터가 날라올 거라 기대하고 그에 따른 멀티쓰레드 관련 문제에 신경을 쓰지 않게 되어 있음.

1.0 이전에는 멀티쓰레드로 구현해 보았으나 오히려 단점이 장점을 덮어버림.!

동시에 여러 Subscriber가 생기는 것은 가능.

```java
main	{
    ExecutorService es = Executors.newSingleThreadExecutor();
    // ...
    
    subscriber.onSubscribe(new Subscription()	{
    	@Override
        public void request(long n)	{
            //Future<?> f = es.execute(() -> { // Subscriber가 작업을 cancel 시켰을 때 Future를 통해서 Interrupt를 날릴 수 있음.
            es.execute(() -> {
                int i = 0;
                try	{
                    while(i++ < n)	{
                    	/* 내용 */
                    }
                } catch (RuntimeException e)	{
                    subscriber.onError(e);
                }
            });
        }
    })
    /* ... */
	es.shutdown();
}
```

이렇게 코드를 완성하면 중간에 에러가 발생해서 onError가 호출 된다. 왜 인지는.. 모르겠다.. ㅎ.. 토비께서는 es.shutdonw() 직전에 es.awaitTermination(10, TimeUnit.HOURS); 메소드를 콜해서 해결. 나는 es.shutdown() 메소드를 onComplete()안에서 호출함으로써 해결.

스펙에는 Subscription을 중복하면 안된다. 캔슬된거 다시하면 안된다. 완료된거 받아선 안된다. 등등이 있지만 여기선 안함.

grpc.io : 두개의 다른 머신에서 데이터 통신하는데 이도 리액티브 방식으로 구현되어 있음.