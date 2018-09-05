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

AsyncRestTemplate은 ListenableFuture라는 스프링의 독자적인 방식으로만 리턴 -> java 표준인 CompletableFuture로 변환해서 콜백 방식을 함수형으로 처리 가능

AsyncRestTemplate : Deprecated 된 이유는 스프링 5의 리액티브 스타일의 코드가 아님. (콜백 사용)

WebClient 로 대체

**thenApplyAsync**  
Async가 없을 경우 non-blocking I/O를 호출 했을 때의 쓰레드를 타고 들어감. (빠르게 결과값만 리턴할 경우)
Async : 새로운 쓰레드로 태움 (복잡한 로직이 들어갈 경우)

**thenAccept**
setResult : 호출한 클라이언트에 결과값을 보내줌.

하고자 하는 바 : api로 가져온 결과값을 다른 api에 보내고 그 결과 값을 로직을 태운뒤 리턴



#### Spring 5.0으로 변환

Mono 혹은 Flux로 return

```java
List<String> 
Optional<String>
Mono<String>
// 하나의 컨테이너로 String을 감싸서 기능을 제공해 줌
```

```java
WebClient client = WebClient.create();

	@GetMapping("rest")
	public Mono<String> rest(int idx) { // Mono 혹은 Flux로 리턴
		Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange(); // 정의만 하는 부분 subscribe 해줘야 함.
//		ClientResponse cr = null; // 1
//		Mono<String> body = cr.bodyToMono(String.class); // 1 
//		Mono<Mono<String>> body = res.map(clientResponse -> clientResponse.bodyToMono(String.class)); //  2다시 컨테이너에 집어넣음 
		Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
		return body;
	}
```

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
@Service
	@Async
	public static class MyService {
		@Async
		public CompletableFuture<String> work(String req) {
			return CompletableFuture.completedFuture(req + "/asyncwork");
		}
	}

@GetMapping("rest")
	public Mono<String> rest(int idx) { // Mono 혹은 Flux로 리턴
		//		Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange(); // 정의만 하는 부분
		////	1.	ClientResponse cr = null;
		////		Mono<String> body = cr.bodyToMono(String.class);
		//	// 2.	Mono<Mono<String>> body = res.map(clientResponse -> clientResponse.bodyToMono(String.class)); // 다시 컨테이너에 집어넣음
		//		Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
		//		return body;

		return client.get().uri(URL1, idx).exchange()               // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class))      // Mono<String>
		             .flatMap((String res1) -> client.get().uri(URL2, res1).exchange())  // Mono<ClientResponse>
		             .flatMap(c -> c.bodyToMono(String.class))     // Mono<String>
            		 .flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2))); // CompletableFuture<String> -> Mono<String>
	}
```



