package mlib.api

import mlib.impl.JsonModuleSystem

object Mlib {
  private var internalSystem: Option[ModuleSystem] = None
  def system = if (internalSystem.isEmpty) {
    internalSystem = Some(new JsonModuleSystem)
    internalSystem.get
  } else internalSystem.get

  def _setSystem(system: ModuleSystem): Unit = internalSystem = Some(system)
}
