package ru.afanasev.embedika.registry.domain.auth.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

final case class SignInRequestDto(
    username: SignInType.Username,
    password: SignInType.Password
)

object SignInType {
    
  type Username = String Refined MatchesRegex["""^[A-Za-z][A-Za-z0-9_]{7,29}$"""]

  type Password = String Refined MatchesRegex["""^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,32}$"""]
}
