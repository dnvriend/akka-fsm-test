package com.example.pingpong

import akka.actor.{Actor, ActorLogging, FSM}
import akka.testkit.TestFSMRef
import com.example.TestSpec

class OnTransistionPingPongTest extends TestSpec {
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

  // When the actor transistions from Ping to Pong, and from Pong to Ping, it
  // will execute some action
  class PingPongFsm extends Actor with ActorLogging with FSM[State, Data] {
    startWith(Ping, NoData)

    when(Ping) {
      case msg @ Event(Ping, _) =>
        log.info("(Ping): {}", msg)
        goto(Pong)
    }

    when(Pong) {
      case msg @ Event(Pong, _) =>
        log.info("(Pong): {}", msg)
        goto(Ping)
    }

    onTransition {
      case Ping -> Pong =>
        log.info("(Ping -> Pong): Transitioning")
      case Pong -> Ping =>
        log.info("(Pong -> Ping): Transitioning")
    }

    whenUnhandled {
      case msg @ Event("PING", _) =>
        log.info("(Unhandled): {}", msg)
        goto(Ping)

      case msg @ Event("PONG", _) =>
        log.info("(Unhandled): {}", msg)
        goto(Pong)
    }

    initialize()
  }

  lazy val fsm = TestFSMRef(new PingPongFsm, s"ping-pong-fsm")

  "PingPong" should "start in Ping state" in {
    fsm.stateName shouldBe Ping
    fsm.stateData shouldBe NoData
  }

  it should "transition to Pong" in {
    fsm ! Ping
    fsm.stateName shouldBe Pong
    fsm.stateData shouldBe NoData
  }

  it should "transition to Ping" in {
    fsm ! Pong
    fsm.stateName shouldBe Ping
    fsm.stateData shouldBe NoData
  }

  it should "transistion to Pong but stay in Pong when receiving 'PONG'" in {
    fsm ! Ping
    fsm.stateName shouldBe Pong
    fsm ! "PONG"
    log.info("Note: no transistion text, it will only fire on a transistion")
  }

  it should "transistion to Ping but stay in Pong when receiving 'Ping'" in {
    fsm ! Pong
    fsm.stateName shouldBe Ping
    fsm ! "PING"
    log.info("Note: no transistion text, it will only fire on a transistion")
  }

  override protected def afterAll(): Unit = {
    cleanup(fsm)
  }
}
