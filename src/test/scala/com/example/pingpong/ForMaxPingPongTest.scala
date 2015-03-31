package com.example.pingpong

import akka.actor.{FSM, ActorLogging, Actor}
import akka.testkit.TestFSMRef
import com.example.TestSpec

class ForMaxPingPongTest extends TestSpec {
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

  // the previous state commands the next state to be in the state, for a maximum
  // duration, like 1 millis here, after which a StateTimeout will be send to the state
  //
  class PingPongFsm extends Actor with ActorLogging with FSM[State, Data] {
    import scala.concurrent.duration._
    startWith(Ping, NoData)

    when(Ping) {
      case msg @ Event((Ping | StateTimeout), _) =>
        log.info("(Ping): {}", msg)
        goto(Pong) forMax (1 millis)
    }

    when(Pong) {
      case msg @ Event((Pong | StateTimeout), _) =>
        log.info("(Pong): {}", msg)
        goto(Ping) forMax (1 millis)
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

  it should "transition to Ping because of the forMax" in {
    eventually {
      fsm.stateName shouldBe Ping
    }
  }

  it should "transition to Pong again because of the forMax" in {
    eventually {
      fsm.stateName shouldBe Pong
    }
  }

  override protected def afterAll(): Unit = {
    cleanup(fsm)
  }
}
