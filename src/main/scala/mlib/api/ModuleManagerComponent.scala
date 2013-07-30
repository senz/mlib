package mlib.api

import akka.actor.ActorRef

/**
 * Application modules manager
 */
trait ModuleManagerComponent {
  val ModuleManager: InnerModuleManager

  trait InnerModuleManager {
    def active(name: String): Boolean
    def get(name: String): ActorRef
    def unload(name: String)
    def list: Set[String]
    def load(name: String)
  }
}
