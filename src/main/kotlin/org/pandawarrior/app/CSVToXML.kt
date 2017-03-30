package org.pandawarrior.app

import com.sun.xml.internal.ws.api.message.HeaderList
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader
import java.io.StringWriter
import java.util.ArrayList
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

/**
 * Created by jtlie on 3/30/2017.
 */
fun csvToDatabase(path: String): List<String>? {

        val fileReader = FileReader(path)
        val format = CSVFormat.newFormat(',').withHeader()
        val csvParser = CSVParser(fileReader, format)
        val headerMap = csvParser.headerMap
        val headerList = headerMap.keys
        //save record line by line
        !checkHeader(headerList.toList())
        lock("focus") { statement ->
            val createHeaderString = headerList.joinToString { "${it} string" }.replace('-', '_')

            val headerString = headerList.joinToString { it }.replace('-', '_')
            statement.executeUpdate("drop table if exists `translation`")
            statement.executeUpdate("create table `translation` (${createHeaderString})")

            for (record: CSVRecord in csvParser) {
                val values = headerMap.values.map {
                    record.get(it)
                }.joinToString { "\"${it}\"" }
                statement.executeUpdate("insert into translation (${headerString}) values(${values})")
            }
        }
        return headerList.toList().subList(2, headerList.toList().lastIndex + 1)


}

fun checkHeader(headerList: List<String>): Boolean {
    if (headerList.get(0) == "name" && headerList.get(1) == "translatable") {
        for (header in headerList.subList(2, headerList.toList().lastIndex + 1)) {
            if (!header.contains("value")) {
                throw Exception("Wrong format: does not contain value-* (example: value, value-zh-CN) column")
            }
        }
        return true
    } else {
        throw Exception("Wrong format: does not contain name or translatable columns")
    }
    return false
}

fun databaseToXML(headerList: List<String>) {

    lock("focus") { statement ->
        for (header in headerList) {
            val head = header.replace('-', '_')
            val cursor = statement.executeQuery("select name, translatable, ${head} from translation")
            val resources = AResounce()
            val stringList = ArrayList<AString>()
            while (cursor.next()) {

                val text = cursor.getString(cursor.findColumn(head))
                val name = cursor.getString(cursor.findColumn("name"))
                val translatable = cursor.getString(cursor.findColumn("translatable"))
                val textString = AString()
                textString.name = name
                textString.text = text
                if (translatable == "false") {
                    textString.translatable = translatable
                }
                if (translatable == "true" || (translatable == "false" && header == "value")) {
                    stringList.add(textString)
                }
            }
            resources.aStringList = stringList
            writeStringResourceXML(resources, header, "$header/string.xml")
        }
    }
}

fun writeStringResourceXML(aResource: AResounce, folderPath: String, filePath: String) {
    val jaxbContext = JAXBContext.newInstance(AResounce::class.java)
    val jaxbMarshaller = jaxbContext.createMarshaller()
    try {
        val file = File(filePath)
        val folder = File(folderPath)
        folder.mkdirs()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        jaxbMarshaller.marshal(aResource, file)
    } catch (error: JAXBException) {
        error.printStackTrace()
    }
}