package ru.afanasev.embedika.registry.utils

import cats.syntax.all._

import doobie.implicits._
import doobie.util.fragment.Fragment

object DoobieUtils {

  def values(fs: Fragment*): Fragment =
    fs.toList.intercalate(fr",")

  def orderBy(fs: String*): Fragment =
    if (fs.isEmpty) Fragment.empty
    else fr"ORDER BY" ++ values(fs.map(Fragment.const(_)): _*)

  def orderByOpt(fs: Option[String]*): Fragment =
    orderBy(fs.toList.unite: _*)
}
