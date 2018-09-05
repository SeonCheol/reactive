package tobyspring.reactive.Reactive08;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@RestController
@Slf4j
@EnableAsync
public class Toby012Application {
    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    // ListenableFuture를 return
    AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

    @Autowired
    MyService myService;

	/* 기본 코드
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
*/
    /* spring 5 */
    //	@Bean
    //	NettyReactiveWebServerFactory nettyReactiveWebServerFactory()   {
    //		return new NettyReactiveWebServerFactory();
    //	}

    WebClient client = WebClient.create();

    @GetMapping("/rest")
    public Mono<String> rest(int idx) { // Mono 혹은 Flux로 리턴

//		Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange();
//
//		Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
//		return body;


        return client.get().uri(URL1, idx).exchange()               // Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class))      // Mono<String>
                .doOnNext(c -> log.info(c.toString()))
                .flatMap((String res1) -> client.get().uri(URL2, res1).exchange())
                .flatMap(c -> c.bodyToMono(String.class))     // Mono<String>
                .doOnNext(c -> log.info(c.toString()))
                .flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2)))
                .doOnNext(c -> log.info(c.toString()));
    }

    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "1");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        SpringApplication.run(Toby012Application.class, args);
    }

//	@Service
//	@Async
//	public static class MyService {
//		public String work(String req) {
//			return req + "/asyncwork";
//		}
//	}

    @Service
    @Async
    public static class MyService {
        @Async
        public CompletableFuture<String> work(String req) {
            return CompletableFuture.completedFuture(req + "/asyncwork");
        }
    }

}
