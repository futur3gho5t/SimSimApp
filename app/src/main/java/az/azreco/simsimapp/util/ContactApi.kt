package az.azreco.simsimapp.util

import android.annotation.SuppressLint
import android.provider.ContactsContract
import az.azreco.simsimapp.SimSimApplication
import az.azreco.simsimapp.model.PhoneContact

object ContactApi {

    fun getContactList(): List<PhoneContact> {
        val context = SimSimApplication.getContext()
        val contactList = mutableListOf<PhoneContact>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cursor?.count ?: 0) > 0) {
            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf<String>(id),
                        null
                    )
                    while (pCursor?.moveToNext() == true) {
                        val phoneNo = pCursor.getString(
                            pCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        ).replace("-", "").replace(" ", "")
                        contactList.add(PhoneContact(name = name, phoneNumber = phoneNo))
                    }
                    pCursor?.close()
                }
            }
            cursor?.close()
        }
        return contactList.distinct()
    }

    @SuppressLint("DefaultLocale")
    fun getContactByName(contactName: String, contactList: List<PhoneContact>): List<PhoneContact> {
        return contactList.filter { i -> i.name.equals(contactName, ignoreCase = true) }
    }

    fun getContactByNumber(
        contactNumber: String,
        contactList: List<PhoneContact>
    ): List<PhoneContact> {
        return contactList.filter { i -> i.phoneNumber == contactNumber }
    }


    fun containsContactName(contactName: String, contactList: List<PhoneContact>): List<PhoneContact> {
        return contactList.filter { i -> i.name.contains(contactName, ignoreCase = true) }
    }

    fun getFilteredPhoneNumber(phoneNo: String): String {
        var phoneNumber = phoneNo
        if (phoneNumber.startsWith("+994")) {
            val countryCode = "0"
            phoneNumber = phoneNumber.replace("+994", "")
            val operatorCode = phoneNumber.take(2)
            phoneNumber = phoneNumber.removeRange(0, 2)
            val firstNumber = phoneNumber.take(3)
            phoneNumber = phoneNumber.removeRange(0, 3)
            val secondNumber = phoneNumber.take(2)
            phoneNumber = phoneNumber.removeRange(0, 2)
            val thirdNumber = phoneNumber.take(2)
            return "$countryCode-$operatorCode-$firstNumber-$secondNumber-$thirdNumber"
        } else {
            val operatorCode = phoneNumber.take(3)
            phoneNumber = phoneNumber.removeRange(0, 3)
            val firstNumber = phoneNumber.take(3)
            phoneNumber = phoneNumber.removeRange(0, 3)
            val secondNumber = phoneNumber.take(2)
            phoneNumber = phoneNumber.removeRange(0, 2)
            val thirdNumber = phoneNumber.take(2)
            return "$operatorCode-$firstNumber-$secondNumber-$thirdNumber"
        }
    }
}