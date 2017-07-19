package controllers

import play.api.mvc.{AbstractController, ControllerComponents}

abstract class CentralController(components: ControllerComponents)
  extends AbstractController(components)
  with PandaAuthActions {
  override val parser           = components.parsers.anyContent
  override val executionContext = components.executionContext
}
