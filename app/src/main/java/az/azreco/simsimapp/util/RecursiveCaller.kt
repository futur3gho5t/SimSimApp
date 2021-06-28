package az.azreco.simsimapp.util

interface RecursiveCaller {

        suspend fun callRecursively(keyWords: String, filterFunc: suspend (String) -> Unit)

}