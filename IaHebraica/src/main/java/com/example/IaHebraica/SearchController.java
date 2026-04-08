package com.example.IaHebraica;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    // O Spring Boot injeta o VectorStore (Qdrant) e o Builder do ChatClient automaticamente
    public SearchController(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Endpoint de Teste do LLM (Ollama)
     * Verifica se o Java consegue fazer a IA "pensar"
     * Acesse: http://localhost:8080/ai/chat?message=Olá
     */
    @GetMapping("/ai/chat")
    public Map<String, String> chat(@RequestParam(value = "message", defaultValue = "Diga que o sistema de IA Hebraica está pronto") String message) {
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            return Map.of("pergunta", message, "resposta_da_ia", response);
        } catch (Exception e) {
            return Map.of("erro", e.getMessage(), "causa", "Verifique se o Ollama baixou o modelo llama3");
        }
    }

    /**
     * Status da conexão
     * Acesse: http://localhost:8080/ping
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Pong");
        status.put("vectorStore", vectorStore.getClass().getSimpleName());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    /**
     * Busca Semântica no Qdrant
     * Acesse: http://localhost:8080/search?query=teste
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(value = "query", defaultValue = "") String query) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(5)
            );
            
            response.put("query", query);
            response.put("resultsCount", results.size());
            response.put("documents", results);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("suggestion", "Verifique o container do Qdrant e se o modelo nomic-embed-text está no Ollama.");
        }
        
        return response;
    }

    @GetMapping("/test")
    public String test() {
        return "Test passed";
    }
}