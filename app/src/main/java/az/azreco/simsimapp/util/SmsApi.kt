package az.azreco.simsimapp.util

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.telephony.SmsManager
import az.azreco.simsimapp.SimSimApplication
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.model.SmsModel

object SmsApi {

    @SuppressLint("DefaultLocale")
    fun getSMSByContactName(
        contactName: String,
        onlyUnread: Boolean,
        contactList: List<PhoneContact>
    ): List<SmsModel> {
        val foundedContacts =
            contactList.filter { p -> p.name.toLowerCase().contains(contactName.toLowerCase()) }
        return if (foundedContacts.isEmpty()) {
            listOf(SmsModel(PhoneContact("", ""), "kantaktı tapa bilmədim"))
        } else {
            val smsList = readSMS(onlyUnread, contactList)
            smsList.filter { sms ->
                sms.phoneContact.name.toLowerCase().contains(contactName.toLowerCase())
            }
        }
    }

    fun readSMS(
        onlyUnread: Boolean,
        contactList: List<PhoneContact>
    ): MutableList<SmsModel> {
        val unreadSMSList = fetchInbox("0", contactList)
        return when (onlyUnread) {
            true -> {
                // only unread SMS
                unreadSMSList
            }
            false -> {
                // unread + read SMS
                unreadSMSList.addAll(fetchInbox("1", contactList))
                unreadSMSList
            }
        }
    }


    @SuppressLint("Recycle")
    private fun fetchInbox(
        readStatus: String,
        contactList: List<PhoneContact>
    ): MutableList<SmsModel> {
        val context = SimSimApplication.getContext()
        val SMS_INBOX = Uri.parse("content://sms/inbox")
        val cursor: Cursor? =
            context.contentResolver.query(SMS_INBOX, null, "read=$readStatus", null, null)
        val smsList = mutableListOf<SmsModel>()
        while (cursor?.moveToNext() == true) {
            val address = cursor.getString(cursor.getColumnIndex("address")).toString()
            val shortAddress = address.replace("+994", "0")
            val body = cursor.getString(cursor.getColumnIndex("body")).toString()
            var name = "naməlum"
            for (i in contactList) {
                if (address == i.phoneNumber || shortAddress == i.phoneNumber) {
                    name = i.name
                }
            }
            val phoneContact = PhoneContact(name = name, phoneNumber = address)
            smsList.add(SmsModel(phoneContact = phoneContact, msg = body))
        }
        return smsList
    }

    fun sendSMSByNumber(phoneNo: String, msg: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("DefaultLocale")
    fun getContactByName(contactName: String, contactList: List<PhoneContact>): List<PhoneContact> {
        return contactList.filter { i -> i.name.equals(contactName, ignoreCase = true) }
    }


    fun sendSMSByName(name: String, msg: String, contactList: List<PhoneContact>) {
        try {
            val foundedContacts =
                getContactByName(contactName = name, contactList = contactList)
            val phoneNo = foundedContacts[0].phoneNumber
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}