package me.jooohn.scalaredux

import Reducer.ops._
import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import akka.stream._
import akka.stream.scaladsl._

object Store {
  type Middleware[S, A] = Flow[A, S, NotUsed] => Flow[A, S, NotUsed]

  def apply[S, A](initialState: S)(implicit R: Reducer[S, A]): Flow[A, S, NotUsed] =
    Flow[A].scan(initialState)(_ dispatch _)

  def withMiddlewares[S, A](middlewares: Middleware[S, A]*)(initialState: S)(implicit R: Reducer[S, A]): Flow[A, S, NotUsed] = {
    middlewares.foldRight(apply(initialState))(_(_))
  }

}
