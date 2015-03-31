package com.example.pingpong

import akka.actor.{Actor, ActorLogging, FSM}
import akka.testkit.TestFSMRef
import com.example.TestSpec

class TimeoutPingPongTest extends TestSpec {
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

  // also, the state machine may stay in one state, for 1 second only, after this one
  // second the actor will receive the StateTimeout message, which it will handle and
  // it will transition to the Pong state

  // In the pong state, it may stay in this state for 1 second, it will receive a
  // StateTimeout, which it will handle and it will transition to the Ping state
  //

  class PingPongFsm extends Actor with ActorLogging with FSM[State, Data] {
    import scala.concurrent.duration._
    startWith(Ping, NoData)

    when(Ping, 1.second) {
      case msg @ Event((Ping | StateTimeout), _) =>
        log.info("(Ping): {}", msg)
        goto(Pong)
    }

    when(Pong, 1.second) {
      case msg @ Event((Pong | StateTimeout), _) =>
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

  it should "automatically transition to Pong" in {
    eventually {
      fsm.stateName shouldBe Pong
    }
  }

  it should "automatically transition to Ping" in {
    eventually {
      fsm.stateName shouldBe Ping
    }
  }

  override protected def afterAll(): Unit = {
    cleanup(fsm)
  }
}
