package tobyspring.reactive.Reactive08;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
