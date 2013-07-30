import mlib.impl.ModuleManagerComponentImpl
import play.api.{Logger, Application, GlobalSettings}
import mlib.impl.MlibGlobal

// Mixin ModuleManager to your registry of cakes
object Registry extends ModuleManagerComponentImpl
{
  val ModuleManager = new InnerModuleManagerImpl
}

// Loading and unloading of modules happens in application Global object, with help of ModuleManager in MlibGlobal trait
object Global extends MlibGlobal {
  val registry = Registry
}
