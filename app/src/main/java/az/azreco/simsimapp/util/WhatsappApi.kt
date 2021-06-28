package az.azreco.simsimapp.util

import android.content.Intent
import az.azreco.simsimapp.SimSimApplication
import az.azreco.simsimapp.model.PhoneContact

object WhatsappApi {

    fun sendMessage(receiver: PhoneContact, text: String) {
        try {
            val context = SimSimApplication.getContext()
            val phoneNo = receiver.phoneNumber.replace("+", "")
            if (!phoneNo.equals("", ignoreCase = true)) {
                val strWhatsAppNo: String = phoneNo // E164 format without '+' sign
                println("strWhatsAppNo: $strWhatsAppNo")
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_TEXT, text)
                intent.putExtra(
                    "jid",
                    "$strWhatsAppNo@s.whatsapp.net"
                ) //phone number without "+" prefix
                intent.setPackage("com.whatsapp")
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}