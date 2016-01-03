package com.assabetsecurity.core.db.data

import org.joda.time.{DateTime, LocalDateTime, LocalDate}
import java.util.UUID
import com.assabetsecurity.core.db._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.DBObject
import scalaz.{Failure, Validation, Success}

import java.security.MessageDigest
import org.apache.commons.codec.binary.Hex
import scalaz._

import scalaz.Failure
import scala.Some
import scalaz.Value
import scalaz.Success


/**
 * User: alyas
 * Date: 4/6/13
 * Time: 3:45 PM
 */

/**
 *internal system login information
 */
case class UserId(value:UUID = UUID.randomUUID())  extends UUIDIdentifier {
  type D = User
  def resource = manifest[D]
}

object UserId  {
  def apply(id:String) = {
    new UserId(UUID.fromString(id))
  }
}
case class UserQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D = User
  def resource = manifest[User]
}

case class UserSecurityRole(role:SecurityRoleEnum.Value = SecurityRoleEnum.Public, expires:Option[DateTime]=None, enabled:Boolean = true)

object SecurityRoleEnum extends Enumeration {
  type SecurityRole = Value
  val Login, Public, MessageAPI, SiteAdmin, SysDBO, SiteOwner, SysAdmin, Download, Demo, SiteModerator = Value
}




case class User  (
  id: UserId,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  loginName: String,
  password: Option[String] = Some("Unknown"),
  authType: String = "Password",
  isActive: Boolean = true,
  expires: DateTime = new DateTime(),
  registrationToken: Option[String] = None,
  registrationTokenExpires: Option[DateTime] = None,
  passwordResetToken: Option[String] = None,
  passwordResetTokenExpires: Option[DateTime] = None,
  identity:Option[UserIdentity] =  None,
  passwordHistory:List[PasswordHistory] = List.empty,
  userSecurityRoles:List[UserSecurityRole]  = List.empty,
  lastSuccessLogin: Option[DateTime] = None
) extends DataRecord {

  def toPublic ={
    this.copy(password=None, passwordHistory=List.empty)
  }

  def nullUserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000000"))

  def updatePassword(oldPassword:Option[String], newPassword:String, sp:SecurityPrincipal):Validation[PasswordUpdateFailure, PasswordUpdateSuccess] = {

    val hash = getPasswordHash(newPassword)

    //TODO: add password complexity/strength check
    if (oldPassword.isDefined) {
      validatePassword(oldPassword.get) match {
        case Success(v:PasswordValidateSuccess) =>
          Success(PasswordUpdateSuccess(
            this.copy(password = Some(hash), passwordHistory = new PasswordHistory(password = hash, modified = new DateTime(), modifiedBy = sp.userId.getOrElse(nullUserId) ) :: this.passwordHistory)
          ))
        case Failure(v:PasswordValidateFailure) =>  Failure(PasswordUpdateFailure(v.id, v.errorCode, v.message))
      }
    } else {
      Success(PasswordUpdateSuccess(
        this.copy(password = Some(hash), passwordHistory = new PasswordHistory(password = hash, modified = new DateTime(), modifiedBy = sp.userId.getOrElse(nullUserId)  ) :: this.passwordHistory)
      ))
    }
  }

  private def getPasswordHash(password: String): String = {
    var passwordSalt = UUID.randomUUID().toString
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest .update((password + ":" + passwordSalt).getBytes("UTF-8"))
    val digest = messageDigest.digest()
    Hex.encodeHexString(digest) + ":" + passwordSalt
  }

  def validatePassword(password: String): Validation[PasswordValidateFailure, PasswordValidateSuccess] = {
    //log.debug("validatePassword for user: " + this.id)
    val res = try {

      val hash = this.password.get.split(':')(0) //get first part of hash string - hash value
      val salt = this.password.get.split(':')(1) //get second part - salt

      val md = MessageDigest.getInstance("SHA-256");

      md.update((password + ":" + salt).getBytes("UTF-8"))

      val digest = md.digest()
      val newPasswordHash = Hex.encodeHexString(digest);

      if (newPasswordHash == hash)
        Success(PasswordValidateSuccess(this.id))
      else
        Failure(PasswordValidateFailure(this.id))
    } catch  {
      case e:Exception => Failure(PasswordValidateFailure(this.id))
    }
    log.debug("validatePassword for user: " + this.id +"::"+ res.isSuccess)
    res
  }
}
object User {
  def nullUserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
}

case class PasswordUpdateSuccess(user:User)
case class PasswordUpdateFailure(id:UserId, errorCode:String="0", message:String = "UnknownError")

case class PasswordValidateSuccess(id:UserId)
case class PasswordValidateFailure(id:UserId, errorCode:String="INVALID_USER_PASSWORD", message:String = "Password Validation Error")



case class UserIdentity(firstName:Option[String]=None, lastName:Option[String] = None, country:Option[String] = None)

case class PasswordHistory(
                       password:String,
                       modified:DateTime = new DateTime(),
                       modifiedBy:UserId)


object AuthType extends Enumeration {
  val Unknown, Password, x509, Token = Value
}

