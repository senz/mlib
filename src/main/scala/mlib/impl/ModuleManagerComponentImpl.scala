package mlib.impl

import scala.collection.mutable
import akka.actor.{Kill, Props, ActorRef}
import mlib.api.{ModuleManagerComponent, ChannelEvent, Module}
import play.api.Play._
import play.api.libs.concurrent.Akka._
import play.api.Logger

trait ModuleManagerComponentImpl extends ModuleManagerComponent {
  private val log = Logger("moduleManager")
  private val modules = mutable.HashMap[String, ActorRef]()

  class InnerModuleManagerImpl extends InnerModuleManager {
    def active(name: String) = modules.get(name).isDefined

    def get(name: String) = modules(name)

    def unload(name: String) {
      get(name) ! Kill
      modules -= name
      log.info(s"unloaded $name")
    }

    def list = modules.keySet.toSet

    def load(name: String) {
      val cls = configuration.getString(s"module.$name.class")
      val listen = configuration.getStringList(s"module.$name.listen")

      if (cls.isEmpty)
        throw new IllegalArgumentException(s"class for module $name is not set")
      if (listen.isEmpty)
        throw new IllegalArgumentException(s"listen for module $name is not set")

      val moduleClass = Class.forName(cls.get)
      val actor = system.actorOf(Props(moduleClass.asInstanceOf[Class[Module]]), name)

      modules += name -> actor
      import collection.JavaConversions._
      for(c <- listen.get) {
        ChannelEvent.subscribe(c, actor)
      }
      log.info(s"loaded $name with channels ${listen.mkString(",")}")
    }
  }
}
