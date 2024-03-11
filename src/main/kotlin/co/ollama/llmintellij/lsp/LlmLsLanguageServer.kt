package co.ollama.llmintellij.lsp

import co.ollama.llmintellij.FimParams
import co.ollama.llmintellij.QueryParams
import co.ollama.llmintellij.TokenizerConfig
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

data class Position(
    val line: Int,
    val character: Int
)
class CompletionParams(
    val prompt: String,
    val model: String,
    val stream: Boolean = false,
)

@JsonSegment("llm-ls")
public interface LlmLsLanguageServer: LanguageServer {
    @JsonRequest
    fun getCompletions(params: CompletionParams): CompletableFuture<CompletionResponse>;
}