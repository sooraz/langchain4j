package ai.staff.rag.config;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LLMConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl("http://localhost:12434/v1") // LM Studio URL
              .modelName("docker.io/ai/deepseek-r1-distill-llama:latest")
                .temperature(0.7)
                .apiKey("dummy-key")
                .timeout(Duration.ofSeconds(120))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
