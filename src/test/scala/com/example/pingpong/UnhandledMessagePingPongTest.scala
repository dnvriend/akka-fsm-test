package com.example.pingpong

import akka.actor.{Actor, ActorLogging, FSM}
import akka.testkit.TestFSMRef
import com.example.TestSpec

class UnhandledMessagePingPongTest extends TestSpec {
  import PingPongFsm._

  // the FSM that we are going to test
  object PingPongFsm {
    sealed trait State
    case object Ping extends State
    case object Pong extends State
    case object Panic extends State

    sealed trait Data
    case object NoData extends Data
  }

  // simple state machine, it start with Ping
  // When in state (Ping), when it receives Ping, the state will transition to Pong
  // When in state (Pong), when it receives Pong, the state will transition to Ping

  // When it receives an unknown message, the message will be handled in the
  // unHandled block, which will set the state of the actor in the state Panic
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

    when(Panic) {
      case msg @ Event(_, _) =>
        log.info("(Panic): {}", msg)
        stay()
    }

    whenUnhandled {
      case msg @ Event(_, _) =>
        log.info("(Unhandled): {}", msg)
        goto(Panic)
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

  it should "transistion to panic state" in {
    fsm ! "This Message Is Unknown"
    fsm.stateName shouldBe Panic
  }

  override protected def afterAll(): Unit = {
    cleanup(fsm)
  }
}
