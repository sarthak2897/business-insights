package modules

import com.google.inject.AbstractModule
import config.AppConfig

class AppModule extends AbstractModule{

  override def configure(): Unit = {
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[BusinessDetailsApplication]).asEagerSingleton()
  }
}
