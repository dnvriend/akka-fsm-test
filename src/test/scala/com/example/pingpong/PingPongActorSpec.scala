package com.example.pingpong

import akka.testkit.TestProbe
import com.example.{PingActor, PongActor, TestSpec}

/**
 * The default ping/pong example that comes with the Activator template,
 * refactored a bit to suit my taste
 */
class PingPongActorSpec extends TestSpec {

  "A Ping actor" should "send back a ping on a pong" in {
    val probe = TestProbe()
    val pingActor = system.actorOf(PingActor.props)
    probe.send(pingActor, PongActor.PongMessage("pong"))
    probe.expectMsg(PingActor.PingMessage("ping"))
    cleanup(pingActor)
  }

  "A Pong actor" should "send back a pong on a ping" in {
    val probe = TestProbe()
    val pongActor = system.actorOf(PongActor.props)
    probe.send(pongActor, PingActor.PingMessage("ping"))
    probe.expectMsg(PongActor.PongMessage("pong"))
    cleanup(pongActor)
  }
}
