/*
 * MIT License
 *
 * Copyright (c) 2017 Liew Jun Tung
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.pandawarrior.app

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.StringReader
import javax.xml.bind.JAXBContext

/**
 * Created by jtlie on 3/14/2017.
 */

class ReadXMLTest {

    val XML_STRING_SAMPLE = """<string name="text_dog">Dog</string>"""
    val XML_RESOURCES_SAMPLE = """<resources>
    <string translatable="true" name="text_dog">Dog</string>
    <string name="text_cat">Cat</string>
    </resources>"""


    @Nested
    @DisplayName("Test AString is working as expected in Kotlin")
    inner class testAString {

        @Test
        @DisplayName("Initialize!")
        fun getAString() {
            val aString:AString = AString()
            assertEquals("", aString.name)
            aString.name = "text_dog"
            aString.text = "Dog"
            assertEquals("text_dog", aString.name)
            assertEquals("Dog", aString.text)
        }
    }

    @Nested
    @DisplayName("Test Read String XML Functionality")
    inner class testReadStringXML {
        @Test
        @DisplayName("Parse from XML String")
        fun parseXml(){
            val jaxbContext = JAXBContext.newInstance(AString::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val reader = StringReader(XML_STRING_SAMPLE)
            val aString = jaxbUnmarchaller.unmarshal(reader) as AString
            assertEquals("text_dog", aString.name)
            assertEquals("Dog", aString.text)
        }
    }

    @Nested
    @DisplayName("Test Read Resource XML Functionality")
    inner class testReadResourcesXML {
        @Test
        @DisplayName("Parse from XML String")
        fun parseXml(){
            val jaxbContext = JAXBContext.newInstance(AResounce::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val reader = StringReader(XML_RESOURCES_SAMPLE)
            val aResources = jaxbUnmarchaller.unmarshal(reader) as AResounce
            assertEquals(2, aResources.aStringList?.size)

            val dogString:AString? = aResources.aStringList?.get(0)
            assertEquals("text_dog", dogString?.name)
            assertEquals("Dog", dogString?.text)
            assertTrue(false)

            val catString:AString? = aResources.aStringList?.get(1)
            assertEquals("text_cat", catString?.name)
            assertEquals("Cat", catString?.text)
            assertNotEquals("text_pig", catString?.name)
            assertNotEquals("Oink", catString?.name)
            if (catString != null){
                assertNull(catString.translatable)
            }
        }
    }
}