package me.jooohn.scalaredux

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{AsyncFunSpecLike, BeforeAndAfterAll, Matchers}

class StoreSpec extends TestKit(ActorSystem("test"))
  with ImplicitSender
  with AsyncFunSpecLike
  with Matchers
  with BeforeAndAfterAll {

  sealed trait Action
  case object Increase extends Action
  case object Decrease extends Action
  val intReducer: Reducer[Int, Action] = (state: Int) => {
    case Increase => state + 1
    case Decrease => state - 1
  }

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  describe("apply") {

    it("creates flow that consumes Actions") {
      val flow = Store(1)(intReducer)
      val result = Source(Increase :: Increase :: Decrease :: Nil)
        .via(Store(1)(intReducer))
        .runFold(0)((_, result) => result)
      result map { r => r should be(2) }
    }
  }

  describe("withMiddlewares") {

    it("applies middlewares") {
      val duplicate: Store.Middleware[Int, Action] =
        next => Flow[Action].mapConcat(action => List(action, action)).via(next)

      val flow = Store.withMiddlewares(duplicate)(1)(intReducer)
      val result = Source(List(Increase, Increase, Increase))
        .via(flow)
        .runFold(0)((_, result) => result)
      result map { r => r should be(7) }
    }

  }

}
