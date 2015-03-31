package com.example.pingpong

import akka.actor.{Actor, ActorLogging, FSM}
import akka.testkit.{TestFSMRef, TestProbe}
import com.example.TestSpec

class ReplyingPingPongTest extends TestSpec {
  import PingPongFsm._

  // the FSM that we are going to test
  object PingPongFsm {
    sealed trait State
    case object Ping extends State
    case object Pong extends State

    sealed trait Data
    case object NoData extends Data
  }

  // simple state machine, it start with Ping
  // When in state (Ping), when it receives Ping, the state will transition to Pong
  // When in state (Pong), when it receives Pong, the state will transition to Ping
  class PingPongFsm extends Actor with ActorLogging with FSM[State, Data] {
    startWith(Ping, NoData)

    when(Ping) {
      case msg @ Event(Ping, _) =>
        log.info("(Ping): {}", msg)
        goto(Pong) replying "GoingToPong"
    }

    when(Pong) {
      case msg @ Event(Pong, _) =>
        log.info("(Pong): {}", msg)
        goto(Ping) replying "GoingToPing"
    }

    initialize()
  }

  lazy val fsm = TestFSMRef(new PingPongFsm, s"ping-pong-fsm")

  "PingPong" should "start in Ping state" in {
    fsm.stateName shouldBe Ping
    fsm.stateData shouldBe NoData
  }

  it should "transition to Pong" in {
    val probe = TestProbe()
    probe.send(fsm, Ping)
    probe.expectMsg("GoingToPong")
    fsm.stateName shouldBe Pong
    fsm.stateData shouldBe NoData
  }

  it should "transition to Ping" in {
    val probe = TestProbe()
    probe.send(fsm, Pong)
    probe.expectMsg("GoingToPing")
    fsm.stateName shouldBe Ping
    fsm.stateData shouldBe NoData
  }

  override protected def afterAll(): Unit = {
    cleanup(fsm)
  }
}
