package az.azreco.simsimapp.util

import android.util.Log
import az.azreco.simsimapp.model.PhoneContact
import java.util.*

object ContactUtil {


    fun azNumerical(): String {
        return "birinci\nikinci\nüçüncü\ndördüncü\nbeşinci\naltıncı\nyeddinci\nsəkkizinci\ndoqquzuncu\nonuncu"
    }

    fun getContactByNumerical(numerical: String, foundedList: List<PhoneContact>): Int {
        return when (numerical) {
            "birinci" -> 0
            "ikinci" -> 1
            "üçüncü" -> 2
            "dördüncü" -> 3
            "beşinci" -> 4
            "altıncı" -> 5
            "yeddinci" -> 6
            "səkkizinci" -> 7
            "doqquzuncu" -> 8
            "onuncu" -> 9
            else -> 0
        }
    }


    fun removeNonAzLetters(names: List<String>): String {
        var counter = 0
        var result = ""
        for (n in names) {
            val l = n.toLowerCase(Locale.getDefault())
            var lol = true
            for (i in l.indices) {
                if (!az_letters.contains(l[i])) {
                    lol = false
                    break
                }
            }
            if (lol) {
                result += l + "\n"
                counter += 1
            }
        }
        return result
    }


    //"a esemes göndər\n"
    // "ya esemes göndər\n"
    fun removeNonAzWithSuffix(names: List<String>,firstSuffix: String,secondSuffix: String): String {
        var result = ""
        for (n in names) {
            val l = n.toLowerCase(Locale.getDefault())
            var lol = true
            for (i in l.indices) {
                if (!az_letters.contains(l[i])) {
                    lol = false
                    break
                }
            }
            if (lol) {
                val last = l.last()
                if (consonants.contains(last)) result += l + firstSuffix
                else if (vowels.contains(last)) result += l + secondSuffix
            }
        }
        Log.d("ContactUtil",result)
        return result
    }


    //ya esemes göndər
    //a esemes göndər
    fun removeSuffix(str: String,firstSuffix: String,secondSuffix: String): String {
        return if (str.contains(firstSuffix)) {
            str.replace(firstSuffix, "")
        } else {
            str.replace(secondSuffix, "")
        }
    }

    private val vowels = listOf(
        'ü',
        'e',
        'y',
        'i',
        'o',
        'ı',
        'a',
        'ə'
    )


    private val consonants = listOf(
        'q',
        'r',
        't',
        'p',
        's',
        'd',
        'f',
        'g',
        'h',
        'j',
        'k',
        'l',
        'z',
        'x',
        'c',
        'v',
        'b',
        'n',
        'm'
    )


    private val az_letters = listOf(
        'ş',
        'ç',
        'm',
        'n',
        'b',
        'v',
        'c',
        'x',
        'z',
        'ə',
        'ı',
        'l',
        'k',
        'j',
        'h',
        'g',
        'f',
        'd',
        's',
        'a',
        'ğ',
        'ö',
        'p',
        'o',
        'i',
        'u',
        'y',
        't',
        'r',
        'e',
        'ü',
        'q',
        ' '
    )
}