package model.command

trait Command {
  type T

  def process()(implicit username: Option[String] = None): Option[T]
}
