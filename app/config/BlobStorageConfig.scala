package config

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlobContainer

import javax.inject.{Inject, Singleton}

@Singleton
class BlobStorageConfig @Inject() (val appConfig : AppConfig) {

  val storageAccount = CloudStorageAccount.parse(appConfig.azureStorageConnectionString)

  val blobClient = storageAccount.createCloudBlobClient()

  val container: CloudBlobContainer = blobClient.getContainerReference(appConfig.azureStorageContainerName)

  def getAzureBlobContainer() = {
    container
  }

}
