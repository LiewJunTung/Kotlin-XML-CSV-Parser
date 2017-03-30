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
import java.io.StringWriter
import java.util.*
import javax.xml.bind.JAXBContext

/**
 * Created by jtlie on 3/14/2017.
 */

class WriteXMLTest {

    private val XML_STRING_SAMPLE = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><string name="text_dog">Dog</string>"""
    private val XML_RESOURCES_SAMPLE = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<resources>
<string name="text_dog" translatable="true">Dog</string>
<string name="text_cat">Cat</string>
</resources>"""

    @Nested
    @DisplayName("Test Write String XML Functionality")
    inner class testWriteStringXML {
        @Test
        @DisplayName("Parse to XML String")
        fun parseXml(){
            val aString = AString()
            aString.name = "text_dog"
            aString.text = "Dog"
            val jaxbContext = JAXBContext.newInstance(AString::class.java)
            val jaxbMarshaller = jaxbContext.createMarshaller()
            val writer = StringWriter()
            jaxbMarshaller.marshal(aString, writer)
            assertEquals(XML_STRING_SAMPLE, writer.toString())
        }
    }

    @Nested
    @DisplayName("Test Write Resource XML Functionality")
    inner class testWriteResourcesXML {
        @Test
        @DisplayName("Parse to XML String")
        fun parseXml(){
            val dogString = AString()
            dogString.name = "text_dog"
            dogString.text = "Dog"
            dogString.translatable = "true"
            val catString = AString()
            catString.name = "text_cat"
            catString.text = "Cat"
            val stringList = ArrayList<AString>()
            stringList.add(dogString)
            stringList.add(catString)
            val resources = AResounce()
            resources.aStringList = stringList
            val jaxbContext = JAXBContext.newInstance(AResounce::class.java)
            val jaxbMarshaller = jaxbContext.createMarshaller()
            val writer = StringWriter()
            jaxbMarshaller.marshal(resources, writer)
            assertEquals(XML_RESOURCES_SAMPLE.replace("\n", "").trim(), writer.toString())
        }
    }

    @Nested
    @DisplayName("Test write a xml file from DB")
    inner class testWriteXMLFile {
        @Test
        fun writeFile(){
            val headers = csvToDatabase("test.csv")
            if (headers == null){
                throw Exception("Invalid headers")
            }
            databaseToXML(headers)
        }
    }
}