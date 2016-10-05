package model.command

import model.User

trait Command {
  type T

  def process()(implicit user: Option[User] = None): Option[T]
}
