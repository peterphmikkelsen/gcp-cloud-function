import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import kotlinx.coroutines.*
import java.net.URL

class CloudFunction: HttpFunction {
    override fun service(request: HttpRequest, response: HttpResponse) = runBlocking {
        val fetchTest = async { fetchData("https://dummyjson.com/test").postLogs() }
        val fetchProducts = async { fetchData("https://dummyjson.com/products").postLogs() }

        withContext(Dispatchers.IO) {
            response.writer.write(fetchTest.await() + "\n\n" + fetchProducts.await())
        }
    }

    private fun fetchData(url: String) = URL(url).readText()

    private fun String.postLogs() = this // TODO: Post as logs to endpoint
}