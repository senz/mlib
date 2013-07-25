package mlib.impl

import play.api.Logger

trait MlibLogger {
  protected val log = Logger("mlib")
}
