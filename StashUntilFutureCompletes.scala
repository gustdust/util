import akka.actor.{Actor, Stash}
import scala.concurrent.Future
import scala.util.{Failure, Success}

class StashUntilFutureCompletes extends Actor with Stash {

  case object StopStash
  case object DoThing
  
  private def doThing = Future.successful() // implement function here

  override def receive: Receive = {
    case DoThing =>
      val future = doThing() pipeTo sender

      //when future finishes stop stashing and unstash all messages
      future.onComplete {
        case Failure(e) => {
          self ! StopStash
        }
        case Success(value) =>  {
          self ! StopStash
        }
      }
      // Stash incoming messages until finished with future,
      context.become(stashing, discardOld = false)
  }

  def stashing: Receive = {
    case StopStash =>
      context.unbecome() // context.unbecome is not thread safe so we cannot call it in the success context of a future
      unstashAll()
    case _ =>
      stash()
  }
}
