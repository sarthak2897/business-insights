package MongoDao

import models.FinalBusinessDetails
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessRepository @Inject() (implicit ec : ExecutionContext,
                                    reactiveMongoApi: ReactiveMongoApi){

  final val logger : Logger = Logger(this.getClass)

  private def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("businesses"))

  def insertBusinessRecords(businessDetails : FinalBusinessDetails) = {
    collection.flatMap(_.insert.one(businessDetails))
  }
}
