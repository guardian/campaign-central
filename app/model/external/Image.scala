package model.external

import ai.x.play.json.Jsonx

case class ImageAsset(imageUrl: String, width: Long, height: Long, mimeType: String)

object ImageAsset {
  implicit val imageAssetFormat = Jsonx.formatCaseClass[ImageAsset]
}

case class Image(imageId: String, assets: List[ImageAsset])

object Image {
  implicit val imageFormat = Jsonx.formatCaseClass[Image]
}
