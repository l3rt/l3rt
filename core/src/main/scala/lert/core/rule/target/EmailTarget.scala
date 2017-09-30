package lert.core.rule.target

import javax.inject.Inject
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import com.typesafe.scalalogging.LazyLogging
import lert.core.config.ConfigProvider
import lert.core.utils.JavaUtils

class EmailTarget @Inject()(configProvider: ConfigProvider) extends LazyLogging {
  def send(recipient: String, subject: String, body: String): Unit = {
    val config = configProvider.config
    require(config.targetSettings != null, "Email server settings are not specified")

    val properties = JavaUtils.toProperties(Map(
      "mail.smtp.host" -> config.targetSettings.mailServer.host,
      "mail.smtp.port" -> config.targetSettings.mailServer.port.toString,
      "mail.smtp.user" -> config.targetSettings.mailServer.username,
      "mail.smtp.password" -> config.targetSettings.mailServer.password,
      "mail.smtp.auth" -> Option(config.targetSettings.mailServer.auth).map(_.toString).orNull,
      "mail.smtp.starttls.enable" -> "true",
      "mail.smtp.socketFactory.class" -> "javax.net.ssl.SSLSocketFactory",
      "mail.smtp.socketFactory.port" -> config.targetSettings.mailServer.port.toString
    ))

    val session = Option(config.targetSettings.mailServer.password)
      .map(p => Session.getDefaultInstance(properties, new Authenticator {
        override def getPasswordAuthentication(): PasswordAuthentication =
          new PasswordAuthentication(config.targetSettings.mailServer.username, p)
      })).getOrElse(Session.getDefaultInstance(properties))

    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(config.targetSettings.mailServer.username))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient))
    message.setSubject(subject)
    message.setText(body)
    Transport.send(message)
  }

  class SMTPAuthenticator extends Authenticator {
    def getPasswordAuthentication(mail: String, password: String) = new PasswordAuthentication(mail, password)
  }

}
