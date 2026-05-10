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
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class RagService {

    private final ChatLanguageModel model;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final Assistant assistant;

    // New LC4j 0.35+ way: Define interface
    interface Assistant {
        String chat(String userMessage);
    }

    public RagService(ChatLanguageModel model) {
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

        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .build();
        
        System.out.println("RAG Service v2 initialized: DeepSeek + Local Embeddings + ContentRetriever");
    }

    public void ingestPdf(MultipartFile file) throws IOException {
        System.out.println("INGEST_START: file={}"+ file.getOriginalFilename());
        var parser = new ApachePdfBoxDocumentParser();
        Document doc = parser.parse(file.getInputStream());
        
        var splitter = DocumentSplitters.recursive(300, 30);
        var segments = splitter.split(doc);
        System.out.println("INGEST_CHUNKS: created {} segments"+ segments.size());
        
        var embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        System.out.println("INGEST_SUCCESS: stored {} embeddings"+ embeddings.size());
    }

    public String ask(String question) {
        System.out.println("RAG_QUERY: {}"+ question);
        String answer = assistant.chat(question);
        System.out.println("RAG_ANSWER: generated response");
        return answer;
    }
}