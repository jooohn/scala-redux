# scala-redux

[redux](https://github.com/reactjs/redux) is an awesome state management library for JavaScript.

This repository is trying to implement some concepts of redux.

## Reducer

```scala
import Reducer.ops._
import Reducer.generic._

// 1. Declare actions.
sealed trait Action
case object EmphasizeTitle extends Action
case class ChangeTitle(title: String) extends Action
case class ChangeBody(body: String) extends Action

// 2. Declare classes representing state with Reducer instances.
case class Header(title: String)
object Header {

  // Reducer is just an wrapper of PartialFunction that receives Action and returns next State.
  implicit val headerStore: Reducer[Header, Action] =
    (state: Header) => {
      case ChangeTitle(title) => state.copy(title = title)
      case EmphasizeTitle => state.copy(title = s"${state.title}!!!")
    }
}

case class Contents(body: String)
object Contents {
  implicit val contentsStore: Reducer[Contents, Action] =
    (state: Contents) => {
      case ChangeBody(body) => state.copy(body = body)
    }
}

// You can even automatically derive reducer from reducers.
case class State(header: Header, contents: Contents)

// 3. Consume actions with reducers.
val initialState = State(
  header = Header("Hello, world!"),
  contents = Contents("initial contents")
)
val actions: List[Action] = List(
  ChangeTitle("Hello, another world!"),
  ChangeBody("new contents"),
  EmphasizeTitle
)
actions.foldLeft(initialState)(_ dispatch _)
// State(
//   header = Header("Hello, another world!!!!"),
//   contents = Contents("new contents")
// )
```

# Store and Middleware

In [redux](https://github.com/reactjs/redux), `store` holds an object to represent current state. In order to implement store's functionality as immutable as possible, Akka Streams `Flow` is used to represent a stream of current states.
```scala
implicit val system: ActorSystem = ActorSystem("example")
implicit val materializer: ActorMaterializer = ActorMaterializer()

sealed trait Action
case object Increase extends Action
case object Decrease extends Action
implicit val intReducer: Reducer[Int, Action] = (state: Int) => {
  case Increase => state + 1
  case Decrease => state - 1
}

// 1. You can write your own Middleware, that receives and returns Flow[Action, State, NotUsed].
val duplicate: Store.Middleware[Int, Action] =
  next => Flow[Action].mapConcat(action => List(action, action)).via(next)
val logger: Store.Middleware[Int, Action] =
  next => Flow[Action]
    .map { action => println(s"action: ${action}"); action }
    .via(next)
    .map { state => println(s"state: ${state}"); state }
  
// 2. Create Flow.
val reduxFlow = Store.withMiddlewares(duplicate, logger)(1)

// 3. Emit Actions and receive States.
val result = Source(List(Increase, Decrease, Increase))
  .via(flow)
  .runFold(0)((_, state) => state)
// state: 1
// action: Increase
// state: 2
// action: Increase
// state: 3
// action: Decrease
// state: 2
// action: Decrease
// state: 1
// action: Increase
// state: 2
// action: Increase
// state: 3
```
