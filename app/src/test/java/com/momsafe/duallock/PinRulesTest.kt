package com.momsafe.duallock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinRulesTest {
    @Test fun acceptsFourToTwelveDigitPins() {
        assertTrue(PinRules.isValid("1234"))
        assertTrue(PinRules.isValid("123456789012"))
    }

    @Test fun rejectsShortLongAndNonNumericPins() {
        assertFalse(PinRules.isValid("123"))
        assertFalse(PinRules.isValid("1234567890123"))
        assertFalse(PinRules.isValid("12a4"))
    }

    @Test fun ownerAndMomPinsMustDiffer() {
        assertTrue(PinRules.areDifferent("4826", "1357"))
        assertFalse(PinRules.areDifferent("4826", "4826"))
    }
}
