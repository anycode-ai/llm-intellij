package co.ollama.llmintellij

import co.ollama.llmintellij.lsp.CompletionParams
import co.ollama.llmintellij.lsp.CompletionResponse
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Job
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
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
        logger.info("sendRequestAsync: $completionParams")
        try {
            return executor.submit<CompletionResponse> {
                val client: HttpClient = DefaultHttpClient()
                val post = HttpPost(url)
                val gson = Gson()
                val input = StringEntity(gson.toJson(completionParams))
                logger.info("sendRequest input: $input")
                input.setContentType("application/json")
                post.entity = input
                logger.info("sending request: $post")
                val response = client.execute(post)
                logger.info("sendRequestAsync response: $response")
                val resp = gson.fromJson(response.entity.content.toString(), CompletionResponse::class.java)
                logger.info("sendRequestAsync response obbj: $resp")
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
