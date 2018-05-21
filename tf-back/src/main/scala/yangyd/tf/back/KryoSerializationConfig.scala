package yangyd.tf.back

import com.romix.akka.serialization.kryo.KryoSerializer
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Configuration for the Kryo serializer.
  * make sure you have
  * `"com.romix.akka.serialization.kryo.KryoSerializationExtension$"`
  * included in `akka.extensions`
  */
object KryoSerializationConfig {
  private val kryoSerializer = classOf[KryoSerializer]
  private val messageTypes = Seq(
    Messages.BackendRegistration.getClass,
    classOf[Messages.JobFailed],
    classOf[Messages.TransformationJob],
    classOf[Messages.TransformationResult]).map(_.getCanonicalName)

  // Example:
  //  serialization-bindings {
  //    "yangyd.tf.back.Messages$BackendRegistration$" = kryo
  //    "yangyd.tf.back.Messages$TransformationJob" = kryo
  //    "yangyd.tf.back.Messages$JobFailed" = kryo
  //  }
  // Scala string interpolation doesn't work for 'escaped "'
  private lazy val bindings = messageTypes.map(String.format("\"%s\" = kryo", _)).mkString("\n")

  lazy val config: Config = ConfigFactory.parseString(
    s"""
       |akka.actor..kryo = ${kryoSerializer.getCanonicalName}
       |akka.actor.kryo.use-manifests = true
       |akka.actor.serialization-bindings {
       |  $bindings
       |}
      """.stripMargin)
}
