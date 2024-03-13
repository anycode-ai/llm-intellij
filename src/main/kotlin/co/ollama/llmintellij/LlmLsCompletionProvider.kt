package co.ollama.llmintellij

import co.ollama.llmintellij.lsp.CompletionParams
import com.google.gson.Gson
import com.intellij.codeInsight.inline.completion.InlineCompletionElement
import com.intellij.codeInsight.inline.completion.InlineCompletionEvent
import com.intellij.codeInsight.inline.completion.InlineCompletionProvider
import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class LlmLsCompletionProvider: InlineCompletionProvider {
    private val logger = Logger.getInstance("inlineCompletion")

    override suspend fun getProposals(request: InlineCompletionRequest): Flow<InlineCompletionElement> =
        channelFlow {
            val project = request.editor.project
            if (project == null) {
                logger.error("could not find project")
            } else {
                val settings = LlmSettingsState.instance
                val secrets = SecretsService.instance
                val lspServer = OllamaServer.getInstance(settings.server)
                logger.info("LSP server found")
                if (lspServer != null) {
                    val caretPosition = request.editor.caretModel.offset
                    val line = request.document.getLineNumber(caretPosition)
                    val lineStart = request.document.getLineStartOffset(line)
                    val lineeEnd = request.document.getLineEndOffset(line)
                    val pre = request.editor.document.getText(TextRange(0, lineStart-1))
                    val suf = request.editor.document.getText(TextRange(lineeEnd, request.editor.document.textLength))
                    val prompt = "<｜fim▁begin｜>" + pre + "<｜fim▁hole｜>" + suf + "<｜fim▁end｜>"//request.editor.document.text//request.editor.document.getText(TextRange(lineStart, lineeEnd))//lspServer.requestExecutor.getDocumentIdentifier(request.file.virtualFile)
                    logger.info("lsp ollama prompt: $prompt")
                    val params = CompletionParams(prompt= prompt, model = settings.model)
                    lspServer.sendRequestAsync(params) { response ->
                        logger.info("lsp ollama response: " + Gson().toJson(response))
                        CoroutineScope(Dispatchers.Default).launch {
                            if (response != null  && response.done == true) {
                                var code = response.response.replace("<｜fim▁begin｜>", "")
                                code = code.replace("<｜fim▁hole｜>", "")
                                code = code.replace("<｜fim▁end｜>", "")
                                send(InlineCompletionElement(code))
                            }
                        }
                    }
                }
            }
            awaitClose()
        }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        val settings = LlmSettingsState.instance
        return settings.ghostTextEnabled
    }
}