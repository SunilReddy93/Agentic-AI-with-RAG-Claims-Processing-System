package com.sunil.claims.decision.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchTool {

    private final VectorStore vectorStore;

    @Tool(description = "Search for similar past insurance claims based on the claim description. " +
            "Use this to find fraud patterns and similar cases before making a decision.")
    public String searchSimilarClaims(String claimDescription) {
        log.info("RAG Search — searching similar claims for: {}", claimDescription);

        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(claimDescription)
                        .topK(5)
                        .similarityThreshold(0.7)
                        .build()
        );

        if (similarDocs == null || similarDocs.isEmpty()) {
            log.info("No similar claims found");
            return "No similar past claims found.";
        }

        String results = similarDocs.stream()
                .map(doc -> "- " + doc.getText() +
                        " [Similarity Score: " + doc.getMetadata().get("score") + "]")
                .collect(Collectors.joining("\n"));

        log.info("Found {} similar claims", similarDocs.size());
        return "Similar past claims found:\n" + results;
    }

    @Tool(description = "Search for relevant insurance policy rules and coverage details " +
            "based on claim type. Use this to verify if the claim is covered.")
    public String searchPolicyRules(String claimType) {
        log.info("RAG Search — searching policy rules for claim type: {}", claimType);

        List<Document> policyDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("insurance policy rules for " + claimType + " claims")
                        .topK(3)
                        .similarityThreshold(0.6)
                        .build()
        );

        if (policyDocs == null || policyDocs.isEmpty()) {
            return "No specific policy rules found for this claim type.";
        }

        return policyDocs.stream()
                .map(doc -> "- " + doc.getText())
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Search for regulatory compliance rules and fraud indicators " +
            "relevant to the claim. Use this to ensure compliance.")
    public String searchComplianceRules(String claimDescription) {
        log.info("RAG Search — searching compliance rules for: {}", claimDescription);

        List<Document> complianceDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("fraud indicators compliance rules " + claimDescription)
                        .topK(3)
                        .similarityThreshold(0.6)
                        .build()
        );

        if (complianceDocs == null || complianceDocs.isEmpty()) {
            return "No specific compliance rules found.";
        }

        return complianceDocs.stream()
                .map(doc -> "- " + doc.getText())
                .collect(Collectors.joining("\n"));
    }
}