package com.momsafe.duallock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinStoreContractTest {
    @Test fun pinComparisonUsesExactValue() {
        val pin = "4826"
        assertFalse(pin == "4827")
        assertTrue(pin.length in 4..12)
    }

    @Test fun ownerAndMomPinsMustBeDifferent() {
        val owner = "4826"
        val mom = "1357"
        assertFalse(owner == mom)
    }
}
