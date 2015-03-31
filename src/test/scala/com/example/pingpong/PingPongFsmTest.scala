package com.example.pingpong

import akka.actor.{Actor, ActorLogging, FSM}
import akka.testkit.TestFSMRef
import com.example.TestSpec


class PingPongFsmTest extends TestSpec {
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
        goto(Pong)
    }

    when(Pong) {
      case msg @ Event(Pong, _) =>
        log.info("(Pong): {}", msg)
        goto(Ping)
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

  override protected def afterAll(): Unit = {
    cleanup(fsm)
  }
}
