package mlib.impl
import mlib.api.{ModuleSystem, Mlib, ModuleManagerComponent}
import play.api.{Application, GlobalSettings}
import collection.JavaConversions._
import collection._

// Loading and unloading of modules happens in application Global object, with help of ModuleManager
trait MlibGlobal extends GlobalSettings {
  val registry: ModuleManagerComponent
  val systemClass: Class[_ <: ModuleSystem] = classOf[JsonModuleSystem]

  override def onStart(app: Application) {
    Mlib._setSystem(systemClass.newInstance())
    val modules = app.configuration.getStringList("modules")
    implicit val a = app

    if (modules.isDefined) loadModules(modules.get)
  }

  override def onStop(app: Application) {
    registry.ModuleManager.list.foreach(registry.ModuleManager.unload)
  }

  private def loadModules(moduleNames: Seq[String])(implicit app: Application) {
    moduleNames.foreach(registry.ModuleManager.load)
  }
}
