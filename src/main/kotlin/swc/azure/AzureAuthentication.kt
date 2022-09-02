package swc.azure

import com.azure.core.credential.TokenCredential
import com.azure.digitaltwins.core.DigitalTwinsClient
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder
import com.azure.identity.AzureCliCredentialBuilder

object AzureAuthentication {

    val authClient = authenticate { AzureCliCredentialBuilder().build() }

    private fun authenticate(builder: () -> TokenCredential): DigitalTwinsClient = DigitalTwinsClientBuilder()
        .credential(builder())
        .endpoint(AzureConstants.DT_SERVICE_ENDPOINT)
        .buildClient()
}
