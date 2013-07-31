package mlib.api

object Mlib {
  private var internalSystem: ModuleSystem = _
  def system = Option(internalSystem).getOrElse(sys.error("ModuleSystem is not set"))

  def _setSystem(system: ModuleSystem): Unit = internalSystem = system
}
