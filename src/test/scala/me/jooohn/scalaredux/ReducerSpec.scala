package me.jooohn.scalaredux

import org.scalatest.{FunSpec, Matchers}

class ReducerSpec extends FunSpec with Matchers {
  import Reducer.ops._
  import Reducer.generic._

  sealed trait Action
  case object EmphasizeTitle extends Action
  case class ChangeTitle(title: String) extends Action
  case class ChangeBody(body: String) extends Action

  case class State(header: Header, contents: Contents)

  case class Header(title: String)
  object Header {
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

  describe("generic") {

    it("should derive Reducers") {
      val initialState = State(
        header = Header("Hello, world!"),
        contents = Contents("initial contents")
      )
      val actions: List[Action] = List(
        ChangeTitle("Hello, another world!"),
        ChangeBody("new contents"),
        EmphasizeTitle
      )
      actions.foldLeft(initialState)(_ dispatch _) should equal(State(
        header = Header("Hello, another world!!!!"),
        contents = Contents("new contents")
      ))
    }

  }

}
