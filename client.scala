import javax.mail._
import javax.mail.internet._
import javax.mail.search._
import java.util.Properties

object ScalaImapSsl {

  def main(args: Array[String]) {
    val props = System.getProperties()
    props.setProperty("mail.store.protocol", "imaps")
    val session = Session.getDefaultInstance(props, null)
    val store = session.getStore("imaps")
    try {
      // use imap.gmail.com for gmail
      store.connect(sys.env("IMAP_SERVER"),
                    sys.env("IMAP_ACCOUNT"), sys.env("IMAP_PASSWORD"))
      val inbox = store.getFolder("Inbox")
      val processed_box = store.getFolder("Processed")
      if (!processed_box.exists()) {
        processed_box.create(Folder.HOLDS_MESSAGES)
        processed_box.setSubscribed(true);
      }
      
      // limit this to 20 message during testing
      inbox.open(Folder.READ_ONLY)
      val messages = inbox.getMessages()
      val limit = 20
      var count = 0
      for (message <- messages) {
        count = count + 1
        if (count > limit) System.exit(0)
        println(message.getSubject())
        if (message.getHeader("List-Unsubscribe") != null) {
          println(message.getHeader("List-Unsubscribe").mkString)
        }
      }
      inbox.close(true)
    } catch {
      case e: NoSuchProviderException =>  e.printStackTrace()
                                          System.exit(1)
      case me: MessagingException =>      me.printStackTrace()
                                          System.exit(2)
    } finally {
      store.close()
    }
  }  
}