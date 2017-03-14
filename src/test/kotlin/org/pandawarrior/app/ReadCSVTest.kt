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

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.util.concurrent.CopyOnWriteArrayList
import java.util.LinkedHashMap



/**
 * Created by jtlie on 3/15/2017.
 */
class ReadCSVTest {
    val CSV = "name,translatable,value,value-zh_CN\n" +
            "text_dog,true,Dog,狗\n" +
            "text_cat,true,Cat,猫"

    @Test
    fun readCSV() {
        val format = CSVFormat.newFormat(',').withHeader()
        val reader = StringReader(CSV)
        val csvParser = CSVParser(reader, format)
        val csvRecords = csvParser.records
        val headerMap = csvParser.headerMap
        val headerList = CopyOnWriteArrayList<Map<String, Int>>()
        val csvInputList = CopyOnWriteArrayList<Map<String, String>>()
        headerList.add(headerMap)
        for (record in csvRecords){
            val inputMap = LinkedHashMap<String, String>()
            for (header in headerMap.entries) {
                inputMap.put(header.key, record.get(header.value))
            }
            if (!inputMap.isEmpty()) {
                csvInputList.add(inputMap)
            }
        }
        csvInputList.forEach(System.out::println)
    }
}