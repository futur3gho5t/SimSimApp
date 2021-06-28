package az.azreco.simsimapp.model

import az.azreco.simsimapp.model.PhoneContact

// Model Of SMS that we receive

data class SmsModel(val phoneContact: PhoneContact, val msg: String)