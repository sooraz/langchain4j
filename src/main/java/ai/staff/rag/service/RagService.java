package ai.staff.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService {

    private final ChatLanguageModel model;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final Assistant assistant;
	private WebSearchEngine tavilyEngine;

    // New LC4j 0.35+ way: Define interface
    interface Assistant {
        String chat(String userMessage);
    }

    public RagService(ChatLanguageModel model,@Value("${langchain4j.tavily.api-key}") String tavilyKey) {
        this.model = model;
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        
        // New ContentRetriever API
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .build();

        
        this.tavilyEngine = TavilyWebSearchEngine.builder()
                .apiKey(tavilyKey)
                .build();
        ContentRetriever webRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(tavilyEngine)
                .maxResults(3)
                .build();
        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .contentRetriever(webRetriever)
                .build();
        
        log.info("RAG Service v2 initialized: DeepSeek + Local Embeddings + ContentRetriever");
    }

    public void ingestPdf(MultipartFile file) throws IOException {
        log.info("INGEST_START: file={}"+ file.getOriginalFilename());
        var parser = new ApachePdfBoxDocumentParser();
        Document doc = parser.parse(file.getInputStream());
        
        var splitter = DocumentSplitters.recursive(300, 30);
        var segments = splitter.split(doc);
        log.info("INGEST_CHUNKS: created {} segments"+ segments.size());
        
        var embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        log.info("INGEST_SUCCESS: stored {} embeddings"+ embeddings.size());
    }

    public String ask(String question) {
        log.info("RAG_QUERY: {}"+ question);
        String answer = assistant.chat(question);
        log.info("RAG_ANSWER: generated response");
        return answer;
    }
    
//    public String ask(String question) {
//        var queryEmbedding = embeddingModel.embed(question).content();
//        var relevant = embeddingStore.findRelevant(queryEmbedding, 5);
//        double topScore = relevant.isEmpty() ? 0.0 : relevant.get(0).score();
//        
//        if (topScore > 0.65) {
//            log.info("PATH: PDF - score={}", topScore);
//            String context = relevant.stream()
//                .map(match -> match.embedded().text())
//                .collect(Collectors.joining("\n\n"));
//            return assistant.chat("Context:\n" + context + "\n\nQuestion: " + question);
//        }
//        
//        log.info("PATH: WEB - PDF score low, searching Tavily");
//        WebSearchResults webResults = tavilyEngine.search(WebSearchRequest.builder()
//                .searchTerms(question)
//                .maxResults(3)
//                .build());
//        
//        String webContext = webResults.results().stream()
//                .map(r -> r.title() + ": " + r.snippet())
//                .collect(Collectors.joining("\n\n"));
//                
//        return chatModel.generate("Web Context:\n" + webContext + "\n\nQuestion: " + question);
//    }
}
    
    
    