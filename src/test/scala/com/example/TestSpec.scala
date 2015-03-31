package com.example

import java.util.UUID

import akka.actor.{PoisonPill, ActorRef, ActorSystem}
import akka.event.{LoggingAdapter, Logging}
import akka.testkit.TestProbe
import org.scalatest.{FlatSpec, Matchers, BeforeAndAfterAll}
import org.scalatest.concurrent.{ScalaFutures, Eventually}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait TestSpec extends FlatSpec with Matchers with BeforeAndAfterAll with Eventually with ScalaFutures {
  implicit val system: ActorSystem = ActorSystem("test")
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 10.seconds)


  def randomId = UUID.randomUUID.toString
  val id = randomId

  def cleanup(actors: ActorRef*): Unit = {
    val probe = TestProbe()
    actors.foreach { actor =>
      probe watch actor
      actor ! PoisonPill
      probe.expectTerminated(actor)
    }
  }
}
