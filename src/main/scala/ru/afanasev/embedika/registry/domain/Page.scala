package ru.afanasev.embedika.registry.domain

import cats.implicits._

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.QueryParamDecoder

object OrderByQueryParam extends OptionalQueryParamDecoderMatcher[String]("orderBy")

object OrderMethodQueryParam extends OptionalQueryParamDecoderMatcher[Methods.Method]("orderMethod")

object FromQueryParam extends OptionalQueryParamDecoderMatcher[Int]("from")

object ToQueryParam extends OptionalQueryParamDecoderMatcher[Int]("to")

final case class Page(
    orderBy: Option[Ordering],
    limit: Int = 10,
    offset: Int = 0
) {
  def ordering =
    orderBy
      .map(orderBy => s"${orderBy.by} ${orderBy.method}")
      .getOrElse("id DESC")
}

object Page {
  def create(
      orderBy: Option[String],
      orderMethod: Option[Methods.Method],
      offset: Option[Int],
      limit: Option[Int]
  ) = Page((orderBy, orderMethod).mapN(Ordering(_, _)), limit.getOrElse(10), offset.getOrElse(0))
}

final case class Ordering(
    by: String,
    method: Methods.Method
)

object Methods extends Enumeration {

  implicit val yearQueryParamDecoder: QueryParamDecoder[Method] =
    QueryParamDecoder[String].map(Methods.withName)

  type Method = Value

  val ASC  = Value("ASC")
  val DESC = Value("DESC")
}
