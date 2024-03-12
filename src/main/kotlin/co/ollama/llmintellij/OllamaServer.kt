package co.ollama.llmintellij

import co.ollama.llmintellij.lsp.CompletionParams
import co.ollama.llmintellij.lsp.CompletionResponse
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Job
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


/**
 * Created by scay on 11.03.2024.
 *
 * @author Şerif Çay
 */
class OllamaServer private constructor(private val url: String) {
    private val logger = Logger.getInstance("OllamaServer")
    fun sendRequestAsync(
        completionParams: CompletionParams,
        callback: (CompletionResponse) -> Job
    ): Future<CompletionResponse>? {
        try {
            logger.info("before sendRequestAsync:" + Gson().toJson(completionParams))
            return executor.submit<CompletionResponse> {
                val request: HttpRequest = HttpRequest.newBuilder()
                    .uri(URI(url))
                    .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(completionParams))
                ).header("Content-Type", "application/json").build();
                val response: HttpResponse<String> = HttpClient.newBuilder()
                    .build()
                    .send(request, BodyHandlers.ofString())
                logger.info("sendRequestAsync response: " + Gson().toJson(response.body()))
                val resp = Gson().fromJson(response.body(), CompletionResponse::class.java)
                logger.info("sendRequestAsync response obbj: "+ Gson().toJson(resp))
                callback.invoke(resp)
                resp
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private var INSTANCE: OllamaServer? = null
        private val executor: ExecutorService = Executors.newFixedThreadPool(3)
        fun getInstance(url: String): OllamaServer? {
            if (INSTANCE == null) {
                INSTANCE = OllamaServer(url)
            } else if (INSTANCE!!.url != url) {
                INSTANCE = OllamaServer(url)
            }
            return INSTANCE
        }
    }
}
