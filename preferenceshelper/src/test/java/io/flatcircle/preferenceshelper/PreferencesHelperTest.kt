package io.flatcircle.preferenceshelper

import org.junit.Test

/**
 * Created by jacquessmuts on 2019-05-24
 */
class PreferencesHelperTest {

    companion object {
        const val bilboString = "{\"age\":111,\"name\":\"Bilbo\"}"
    }

    val rightClass = TestyClass("Bilbo", 111)
    val wrongClass = TestyClass("Frodo", 35)

    @Test
    fun serializeIntoString() {

        val rightString = PreferencesHelper.serializeIntoString(rightClass)
        assert(rightString == bilboString)

        val wrongString = PreferencesHelper.serializeIntoString(wrongClass)
        assert(wrongString != bilboString)
    }

    @Test
    fun serializeFromString() {

        val deserializedClass = PreferencesHelper.serializeFromString(bilboString, wrongClass)
        assert(rightClass == deserializedClass)

        val defaultClass = PreferencesHelper.serializeFromString("", rightClass)
        assert (defaultClass == rightClass)
    }

    data class TestyClass(val name: String, val age: Int)

}