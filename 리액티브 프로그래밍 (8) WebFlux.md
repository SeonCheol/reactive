### 리액티브 프로그래밍 (8) WebFlux



#### 기본 코드

```java
@SpringBootApplication
public class RemoteService {
//	@Bean
//	TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory()     {
//		return new TomcatReactiveWebServerFactory();
//	};

	@RestController
	public static class MyController {
		@GetMapping("/service")
		public Mono<String> service(String req) throws InterruptedException {
			Thread.sleep(1000);
			return Mono.just(req + "/service1");
		}

		@GetMapping("/service2")
		public Mono<String> service2(String req) throws InterruptedException {
			Thread.sleep(1000);
			return Mono.just(req + "/service2");
		}
	}

	public static void main(String[] args)  {
		System.setProperty("server.port", "8081");
		System.setProperty("server.tomcat.max-threads", "1000");
		SpringApplication.run(RemoteService.class, args);
	}

}

```



```java
/** api 결과 -> api 결과 -> service 처리 후 반환 *//
@SpringBootApplication
@RestController
@Slf4j
public class Toby012Application {
	static final String URL1 = "http://localhost:8081/service?req={req}";
	static final String URL2 = "http://localhost:8081/service2?req={req}";

	// ListenableFuture를 return 
	AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

	@Autowired
	MyService myService;

	@GetMapping("/rest")
	public DeferredResult<String> rest(int idx) {
		DeferredResult<String> dr = new DeferredResult<>();

		toCF(rt.getForEntity(URL1, String.class, "h" + idx))
			.thenCompose(s -> toCF(rt.getForEntity(URL2, String.class, s.getBody())))
			.thenApplyAsync(s2 -> myService.work(s2.getBody()))
			.thenAccept(s3 -> dr.setResult(s3))
			.exceptionally(e -> {
				dr.setErrorResult(e.getMessage());
				return (Void)null;
			});
		return dr;
	}

	<T> CompletableFuture<T> toCF(ListenableFuture<T> lf) {
		CompletableFuture<T> cf = new CompletableFuture<T>();
		lf.addCallback(s -> cf.complete(s), e -> cf.completeExceptionally(e));
		return cf;
	}

	public static void main(String[] args) {
		System.setProperty("reactor.ipc.netty.workerCount", "2");
		System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
		SpringApplication.run(Toby012Application.class, args);
	}

	@Service
	public static class MyService {
		public String work(String req) {
			return req + "/asyncwork";
		}
	}

}

```

**getForEntity**
http 응답을 통째로 return 하므로 *getBody()*를 통해 body 만 가져와서 사용.

**AsyncRestTemplate**
*ListenableFuture*라는 스프링의 독자적인 방식으로만 리턴 -> java 표준인 *CompletableFuture*로 변환해서 콜백 방식을 함수형으로 처리 가능

스프링 5의 리액티브 스타일 코드가 아님(콜백 사용) -> *WebClient* 로 대체

**thenApplyAsync**  
Async가 없을 경우 non-blocking I/O를 호출 했을 때의 쓰레드를 타고 들어감. (빠르게 결과값만 리턴할 경우)
Async : 새로운 쓰레드로 태움 (복잡한 로직이 들어갈 경우)

**thenAccept**
setResult : 호출한 클라이언트에 결과값을 보내줌.



#### Spring 5.0으로 변환

Mono 혹은 Flux로 return

**Mono**
0-1 개의 결과만을 처리하기 위한 Reactor의 객체. 여러 스트림을 하나의 결과로 모아줄 때 Mono를 사용

**Flux**
0-N 개의 여러 개의 결과를 처리하는 객체. 각각의 Mono를 합쳐서 여러 개의 값을 처리하는 Flux로 표현.

**공통점**
Reactive Stream의 Publisher 인터페이스를 구현하고 있으며, Reactor에서 제공하는 풍부한 연산자들의 조합을 통해 스트림을 표현 할 수 있음.

```java
List<String> 
Optional<String>
Mono<String>
// 하나의 컨테이너로 String을 감싸서 기능을 제공해 줌
```

```java
WebClient client = WebClient.create();

	@GetMapping("/rest")
	public Mono<String> rest(int idx) { // Mono 혹은 Flux로 리턴
        // return Mono.just("Hello"); /* 정해진 값 리턴 */
        
		Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange(); // 정의만 하는 부분 subscribe 해줘야 함. 
//		ClientResponse cr = null; // 1
//		Mono<String> body = cr.bodyToMono(String.class); // 1 
//		Mono<Mono<String>> body = res.map(clientResponse -> clientResponse.bodyToMono(String.class)); //  2다시 컨테이너에 집어넣음 
		Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
		return body;
	
```

*client.get().uri(URL1, idx).exchange();* 만으로는 return 값이 생성되지 않음. 

Mono는 Publisher 인터페이스를 구현하고 있으므로 subscribe() 를 해줘야 실제로 return 값을 생성함. 

스프링이 알아서 해줌.

```java
	@GetMapping("rest")
	public Mono<String> rest(int idx) { 
		return client.get().uri(URL1, idx).exchange().flatMap(c -> c.bodyToMono(String.class));
	}
```

이 코드가 AsyncRestTemplate + DeferredResult 처럼 동작하는가?
 -> 토비의 대답 : Yes! 다 해봤다고 하심.

##### api 결과 값을 다른 api 파라미터로 콜

```java
@GetMapping("rest")
	public Mono<String> rest(int idx) { 
		return client.get().uri(URL1, idx).exchange()               // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class))      // Mono<String>
		             .flatMap((String res1) -> client.get().uri(URL2, res1).exchange())  // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class));     // Mono<String>
	}
```

##### myService 추가

```java
return client.get().uri(URL1, idx).exchange()               // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class))      // Mono<String>
		             .flatMap((String res1) -> client.get().uri(URL2, res1).exchange())  // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class))     // Mono<String>
//		.map(res2 -> myService.work(res2));
//		.flatMap(res2 -> myService.work(res2));
```



만약 MyService 의 work 가 시간이 오래 걸리는 작업일 때 work를 또 다른 쓰레드에서 비동기 작업으로 처리

```java
@SpringBootApplication
@RestController
@Slf4j
@EnableAsync
public class Toby012Application {	
	
    @Service
	@Async
	public static class MyService {
		@Async
		public CompletableFuture<String> work(String req) { // Future, ListenableFuture
			return CompletableFuture.completedFuture(req + "/asyncwork");
		}
	}

@GetMapping("/rest")
public Mono<String> rest(int idx) { // Mono 혹은 Flux로 리턴
	return client.get().uri(URL1, idx).exchange()               // Mono<ClientResponse>
	             .flatMap(c -> c.bodyToMono(String.class))      // Mono<String>
	             .flatMap((String res1) -> client.get().uri(URL2, res1).exchange())  // mono<ClientResponse>
	             .flatMap(c -> c.bodyToMono(String.class))     // Mono<String>
           		 .flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2))); // CompletableFuture<String> -> Mono<String>
	}
}
```



> 웹플럭스의 스타일로 프로젝트를 만들고 싶으면 비지니스 로직 모두가 @Async 같은 것을 이용해서 비동기 방식으로 구현이 되어야 하나요? 

```java
@GetMapping("/rest")
	public Mono<String> rest(int idx) { // Mono 혹은 Flux로 리턴
		return client.get().uri(URL1, idx).exchange()               // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class))      // Mono<String>
            		 .doOnNext(c -> log.info(c.toString()))
		             .flatMap((String res1) -> client.get().uri(URL2, res1).exchange())  
		             .flatMap(c -> c.bodyToMono(String.class))     // Mono<String>
            		 .doOnNext(c -> log.info(c.toString()))
            		 .flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2))); 
        			 .doOnNext(c -> log.info(c.toString()))
	}
```

추상화 잘 되어 있으나 많이 익숙해져야 한다. (어떤 쓰레드를 타는지, 언제 subscribe() 되는지 등등 ..)

-> 새로운 쓰레드를 태울지, 기존의 쓰레드에서 로직을 수행할 것인지 잘 설계 해야 함.  즉, 항상 위의 질문 같이 그렇게 구현할 필요는 없다.



추천 책 : rxJava를 이용한 비동기 프로그래밍