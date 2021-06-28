package az.azreco.simsimapp.exoplayer

interface ExoListener {
    fun onEndState(lambda: () -> Unit)
}