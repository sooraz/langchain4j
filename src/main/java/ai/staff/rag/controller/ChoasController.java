//package ai.staff.rag.controller;
//
//import java.time.Duration;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.server.ResponseStatusException;
//
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import lombok.extern.slf4j.Slf4j;
//
//@RestController
//@RequestMapping("/chat")
//@Slf4j
//public class ChoasController {
//
//    private final AtomicInteger errorCount = new AtomicInteger(0);
//    private final Counter errorCounter;
//    private final OpenAiChatModel model;
//
//    public ChoasController(MeterRegistry registry) {
//        this.errorCounter = Counter.builder("rag_errors_total")
//                .tag("type", "llm_timeout")
//                .register(registry);
//        
//        this.model = OpenAiChatModel.builder()
//                .baseUrl("http://localhost:12434/v1")
//                .apiKey("docker") // local ki emaina ok
//                .modelName("docker.io/ai/deepseek-r1-distill-llama:latest")
//                .timeout(Duration.ofSeconds(120))
//                .temperature(0.1) // R1 reasoning model ki low temp better
//                .logRequests(true)
//                .build();
//        
////        log.info("DeepSeek R1 connected on 12434");
//        System.out.println("DeepSeek R1 connected on 12434");
//    }
//
//    @PostMapping("/ask")
//    public String ask(@RequestBody String question) {
//        int count = errorCount.incrementAndGet();
//        
//        if (count <= 6) {
//            errorCounter.increment();
//            System.out.println("ERROR_ERROR_COUNT="+count+"} : Faking LLM timeout");
//            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
//                    "Error error bro: LLM down. Count: " + count);
//        }
//        
//        // Step 2: Ikkada asalu LangChain4j use chestunam
//        System.out.println("SUCCESS_COUNT={} : Calling LLM"+count);
//        String answer = model.generate("10+ yrs staff engineer answer: " + question);
//        return answer;
//    }
//    
//    @PostMapping("/reset")
//    public String reset() {
//        errorCount.set(0);
//        return "Reset done";
//    }
//}
