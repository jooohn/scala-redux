# scala-redux

[redux](https://github.com/reactjs/redux) is an awesome state management library for JavaScript.

This repository is trying to implement some concepts of redux.

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
