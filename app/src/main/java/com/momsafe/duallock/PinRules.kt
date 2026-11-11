package com.momsafe.duallock

internal object PinRules {
    fun isValid(pin: String): Boolean = pin.length in 4..12 && pin.all { it in '0'..'9' }
    fun areDifferent(ownerPin: String, momPin: String): Boolean = ownerPin != momPin
}
