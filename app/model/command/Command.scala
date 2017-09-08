package model.command

import model.User
import repositories.{CampaignContentRepository, CampaignRepository, ClientRepository}

trait Command {
  type T

  def process(
    campaignRepository: CampaignRepository,
    campaignContentRepository: CampaignContentRepository,
    clientRepository: ClientRepository
  )(implicit user: Option[User] = None): Either[CommandError, Option[T]]
}
