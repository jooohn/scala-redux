package me.jooohn.scalaredux

import shapeless.{::, Generic, HList, HNil}

trait Reducer[S, A] {

  def reduce(state: S): PartialFunction[A, S]

  def dispatch(state: S, action: A): S = reduce(state).applyOrElse(action, (_: A) => state)

}
object Reducer {
  object generic {

    implicit def hnilReducer[A]: Reducer[HNil, A] =
      (_: HNil) => PartialFunction.empty

    implicit def hlistReducer[S, T <: HList, A](
      implicit
      hReducer: Reducer[S, A],
      tReducer: Reducer[T, A]
    ): Reducer[S :: T, A] = (state: S :: T) => {
      case action =>
        state match {
          case s :: t => hReducer.dispatch(s, action) :: tReducer.dispatch(t, action)
        }
    }

    implicit def genericReducer[S, A, R](
      implicit
      gen: Generic[S] { type Repr = R },
      reducer: Reducer[R, A]
    ): Reducer[S, A] = (state: S) => {
      case action => gen.from(reducer.dispatch(gen.to(state), action))
    }

  }

  object ops {
    implicit class ReducerOps[S](s: S) {

      def dispatch[A](action: A)(implicit S: Reducer[S, A]): S = S.dispatch(s, action)

    }
  }

}
