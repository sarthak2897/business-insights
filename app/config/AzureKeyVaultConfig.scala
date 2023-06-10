package config

import javax.inject.{Inject, Singleton}
import com.azure
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClientBuilder

@Singleton
class AzureKeyVaultConfig @Inject() (appConfig : AppConfig)  {

  val vaultUrl = appConfig.azureKeyVaultUrl
  val credential = new DefaultAzureCredentialBuilder().build()

  val client = new SecretClientBuilder()
    .vaultUrl(vaultUrl)
    .credential(credential)
    .buildClient()

  def fetchKeyVaultClient() = {
    client
  }

}
