package swc.azure

import com.azure.core.credential.TokenCredential
import com.azure.core.models.JsonPatchDocument
import com.azure.digitaltwins.core.DigitalTwinsClient
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder
import com.azure.identity.AzureCliCredentialBuilder
import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.cdimascio.dotenv.dotenv

object AzureAuthentication {

    val authClient = authenticate { AzureCliCredentialBuilder().build() }
    lateinit var queueProcessor: ServiceBusProcessorClient
    fun queueProcessorClient(
        processMethod: (context: ServiceBusReceivedMessageContext) -> Unit
    ): ServiceBusProcessorClient = ServiceBusClientBuilder()
        .connectionString(dotenv()["SERVICE_BUS_CONNECTION_STRING"])
        .processor()
        .topicName("dumpsters-topic")
        .subscriptionName("dumpsters-subscription")
        .processMessage { processMethod(it) }
        .processError{
            println("Error when receiving messages from namespace: ${it.fullyQualifiedNamespace}. " +
                    "Entity: ${it.entityPath}." +
                    "Reason: ${it.exception.cause}")
        }
        .buildProcessorClient()
        .also { queueProcessor = it }

    private fun authenticate(builder: () -> TokenCredential): DigitalTwinsClient = DigitalTwinsClientBuilder()
        .credential(builder())
        .endpoint(AzureConstants.DT_SERVICE_ENDPOINT)
        .buildClient()
}
