package co.ollama.llmintellij.lsp

class Completion(val response: String)

class CompletionResponse {
    val request_id: String = ""
    val completions: List<Completion> = emptyList()
}