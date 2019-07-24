import javax.mail._
import javax.mail.internet._
import javax.mail.search._
import java.util.Properties

object ScalaImapSsl {

    def prepareMailbox(store: Store) {
        val processed_box = store.getFolder("Processed")
        if (!processed_box.exists()) {
            processed_box.create(Folder.HOLDS_MESSAGES)
            processed_box.setSubscribed(true);
        }
    }

    def main(args: Array[String]) {
        val props = System.getProperties()
        props.setProperty("mail.store.protocol", "imaps")
        val session = Session.getDefaultInstance(props, null)
        val store = session.getStore("imaps")
        try {
            store.connect(sys.env("IMAP_SERVER"),
                          sys.env("IMAP_ACCOUNT"), sys.env("IMAP_PASSWORD"))
            prepareMailbox(store)
            val inbox = store.getFolder("Inbox")

            val total_messages = inbox.getMessageCount()
            val unread_messages = inbox.getNewMessageCount()
            println(f"Total $total_messages ($unread_messages unread)")

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
            case e: NoSuchProviderException => e.printStackTrace()
                System.exit(1)
            case me: MessagingException => me.printStackTrace()
                System.exit(2)
        } finally {
            store.close()
        }
    }
}