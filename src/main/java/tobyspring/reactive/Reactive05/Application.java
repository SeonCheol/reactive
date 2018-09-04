package tobyspring.reactive.Reactive05;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {
    @RestController
    public static class MyController {
        RestTemplate rt = new RestTemplate();

        @GetMapping("/rest")
        public String rest(int idx) {
            String res = rt.getForObject(
                    "http://localhost:8081/service?req={req}", String.class, "hello" + idx);
            return "rest" + idx + res;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
