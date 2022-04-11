package ru.afanasev.embedika.registry.domain.history.repository.model

import java.time.LocalDateTime
import doobie.util.Get
import doobie.util.Put
import org.http4s.QueryParamDecoder
import io.circe.Decoder
import io.circe.Encoder

trait HistoryEntity

object HistoryEntity {

  final case class NewHistoryEntity(
      historyType: HistoryType.Type,
      userId: Option[Long],
      result: HistoryResult.Result,
      executionTime: Long,
  ) extends HistoryEntity

  final case class ExistingHistoryEntity(
      id: Long,
      historyType: HistoryType.Type,
      userId: Option[Long],
      result: HistoryResult.Result,
      executionTime: Long,
      dateAdded: LocalDateTime
  ) extends HistoryEntity
}

object HistoryType extends Enumeration {

  type Type = Value

  val SIGN_UP  = Value("SUGN_UP")
  val SIGN_IN  = Value("SIGN_IN")
  val REFRESH  = Value("REFRESH")
  val ADD_CAR  = Value("ADD_CAR")
  val DEL_CAR  = Value("DEL_CAR")
  val GET_CARS = Value("GET_CARS")
  val GET_STAT = Value("GET_STAT")

  implicit val typeGet: Get[Type] = Get[String].map(HistoryType.withName(_))

  implicit val typePut: Put[Type] = Put[String].contramap(_.toString)

  implicit val typeQueryParamDecoder: QueryParamDecoder[Type] =
    QueryParamDecoder[String].map(HistoryType.withName)

  implicit val typeDecoder: Decoder[HistoryType.Value] =
    Decoder.decodeEnumeration(HistoryType)

  implicit val typeEncoder: Encoder[HistoryType.Value] =
    Encoder.encodeEnumeration(HistoryType)
}

object HistoryResult extends Enumeration {

  type Result = Value

  val SUCCESS        = Value("SUCCESS")
  val ALREADY_EXISTS = Value("ALREADY_EXISTS")
  val FAILED         = Value("FAILED")
  val NOT_FOUND      = Value("NOT_FOUND")

  implicit val typeGet: Get[Result] = Get[String].map(HistoryResult.withName)

  implicit val typePut: Put[Result] = Put[String].contramap(_.toString)

  implicit val typeQueryParamDecoder: QueryParamDecoder[Result] =
    QueryParamDecoder[String].map(HistoryResult.withName)

  implicit val genderDecoder: Decoder[HistoryResult.Value] =
    Decoder.decodeEnumeration(HistoryResult)

  implicit val genderEncoder: Encoder[HistoryResult.Value] =
    Encoder.encodeEnumeration(HistoryResult)
}
