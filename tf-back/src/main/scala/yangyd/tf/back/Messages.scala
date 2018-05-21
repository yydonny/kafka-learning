package yangyd.tf.back

object Messages {
  final case class TransformationJob(text: String)
  final case class TransformationResult(text: String)
  final case class JobFailed(reason: String, job: TransformationJob)
  case object BackendRegistration
}
