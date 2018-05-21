package yangyd.tf.back

import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]): Unit = {

    println(ConfigFactory.load())

    args.headOption match {
      case Some(mode) ⇒ mode match {
        case "front" ⇒
          TransformationFrontend.startup(Seq("2552").toArray)
          TransformationFrontend.startup(Array.empty)
          TransformationFrontend.startup(Array.empty)
        case "back" ⇒
          TransformationBackend.startup(Seq("2551").toArray)
          TransformationBackend.startup(Array.empty)
        case _ ⇒
          throw new IllegalArgumentException("front or back")
      }
      case _ ⇒
        throw new IllegalArgumentException("front or back")
    }
  }
}
