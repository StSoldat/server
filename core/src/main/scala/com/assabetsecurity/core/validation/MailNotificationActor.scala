package com.assabetsecurity.core.validation

import java.util.Properties
import javax.mail.{PasswordAuthentication, Transport, Message, Session}
import javax.mail.internet.{InternetAddress, MimeMessage}

import akka.actor.{ActorLogging, Props, Actor}
import org.joda.time.DateTime
import org.slf4s.Logging

/**
 * Created by alyas on 7/8/15.
 */
class MailNotificationActor extends Actor with Logging {
  /*smtp.sendgrid.net:587
  ohavryl@gmail.com  */
  override def receive: Receive = {
    case d:MailData => {
      try {
        log.info(s"Send message for ${d.sendTo}")
        val props = new Properties()
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.host", "xjdz4.dailyrazor.com")
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", "465")
        props.put("mail.smtp.timeout", "2000");

        val session = Session.getInstance(props,
          new javax.mail.Authenticator() {

            override def getPasswordAuthentication():PasswordAuthentication  = {
               new PasswordAuthentication("service@assabetsecurity.com", "sdfr23432");
            }

          });

        session.setDebug(true)
        val message = new MimeMessage(session)

        message.setRecipient(Message.RecipientType.TO, new InternetAddress(d.sendTo))
        message.setRecipient(Message.RecipientType.BCC, new InternetAddress("alyas77@gmail.com"))

        message.setFrom(new InternetAddress("noreply@canmoderate.com"))

        message.setSubject(d.subject.getOrElse("CanModerate.com"))

        message.setContent(
          (<div>
            <div>{DateTime.now().toString()}</div>
            <p>{d.body.getOrElse("")}</p>
          </div>).toString()
          , "text/html; charset=utf-8")

        log.info("SMTP >> send... ")

        Transport.send(message);
        log.info(s"SMTP >> send complete. ${d.sendTo}")

      } catch {
        case e:Throwable=> log.error("SMTP Error"+e.toString)
      }
    }
    case _=>{

    }
  }
}

case class MailData(sendTo:String, subject:Option[String], body:Option[String])