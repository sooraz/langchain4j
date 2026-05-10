package ai.staff.rag.config;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LLMConfig {

	@Value("${chatModel.baseURL}")
	private String baseUrl;


	@Value("${chatModel.model}")
	private  String model;

	@Value("${chatModel.apiKey}")
	private String apiKey;

	@Value("${chatModel.temperature}")
	private double temperature;

	@Value("${chatModel.timeout}")
	private int timeout;
	
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl) // LM Studio URL
              .modelName(model)
                .temperature(temperature)
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
