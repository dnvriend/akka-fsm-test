akka-fsm-test
=============
A small study project that shows how a very simple example, the PingPong FSM, that transitions only
from the state Ping, to the state Pong (everyone understands that), and shows several ways for the FSM
to go from state Ping -> Pong. It shows the following:

* PingPongActorSpec: A refactored test suited to my testing taste of the vanilla PingPong project that comes
with Activator
* PingPongFsmTest: The FSM goes from state `Ping -> Pong` on a received message `Ping` and from state `Pong -> Ping` on
 message `Pong`
* UnhandledMessagePingPongTest: The same, but the actor goes into `Panic` state when it receives a message that it
does not understand
* TimeoutPingPongTest: The same, but the state has a timeout of one second, on which it receives a `StateTimeout`
which it handles and transitions to the other state either `Ping` or `Pong`
* OnTransitionPingPongTest: The same, but shows that on the transition the FSM can also execute tasks. Note that
the onTransition block only fires on state changes, and not when the state stays the same.
* ReplyingPingPongTest: The same, only it replies with a message to the sender. Note that the same rules apply
for a 'normal' actor. The last message received is the sender() reference, so when the FSM sends a message to itself
or it receives a message from another actor (maybe a worker), then the replying will use that ActorRef.
* ForMaxPingPongTest: The same, but the FSM may stay in a certain state for a maximum amount of time the forMax,
and then received a `StateTimeout`, which it handles and transitions to the other state either `Ping` or `Pong`

Have fun!
