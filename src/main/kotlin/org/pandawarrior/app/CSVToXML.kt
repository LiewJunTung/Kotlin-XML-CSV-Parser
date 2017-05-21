package org.pandawarrior.app

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

/**
 * Created by jtlie on 3/30/2017.
 */
fun stringCsvToDatabase(path: String): List<String>? {
    val dbName = "build/xml_translation"
    val tableName = "translation"
    return csvToDatabase(path, dbName, tableName, true)
}

fun pluralsCsvToDatabase(path: String): List<String>? {
    val dbName = "build/xml_translation"
    val tableName = "plural_translation"
    return csvToDatabase(path, dbName, tableName, false)
}

fun arraysCsvToDatabase(path: String): List<String>? {
    val dbName = "build/xml_translation"
    val tableName = "arrays_translation"
    return csvToDatabase(path, dbName, tableName, false)
}

fun csvToDatabase(path: String, dbName: String, tableName: String, isString: Boolean): List<String> {
    val fileReader = FileReader(path)
    val format = CSVFormat.DEFAULT.withHeader()
    val csvParser = CSVParser(fileReader, format)
    val headerMap = csvParser.headerMap
    val headerList = headerMap.keys
    //save record line by line
    !checkHeader(headerList.toList(), isString)
    try {
        lock(dbName, tableName) { statement, connection ->
            val createHeaderString = headerList.joinToString { "${it} string" }.replace('-', '_')

            val headerString = headerList.joinToString { it }.replace('-', '_')
            statement.executeUpdate("drop table if exists `$tableName`")
            statement.executeUpdate("create table `$tableName` (${createHeaderString})")

            for (record: CSVRecord in csvParser) {
                val hValues = headerMap.values.map {
                    record.get(it)
                }
                val pholder = hValues.map {
                    "?"
                }.joinToString { it }
                val stmt = connection.prepareStatement("insert into $tableName (${headerString}) values($pholder)")
                for ((index, value) in hValues.withIndex()) {
                    stmt.setString(index + 1, value)
                }
                stmt.executeUpdate()
            }
        }
    } catch (e: ArrayIndexOutOfBoundsException) {
        throw InvalidSourceException("Invalid CSV format, check if there are any incomplete data.")
    }
    return headerList.toList().subList(1, headerList.toList().lastIndex + 1)
}

fun checkHeader(headerList: List<String>, isString: Boolean = true): Boolean {
    if (headerList.get(0) == "name" &&
            (isString && headerList.get(1) == "translatable" || !isString)) {
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

fun databaseToStringXML(headerList: List<String>) {
    val tableName = "translation"
    val dbName = "build/xml_translation"
    lock(dbName, tableName) { statement, connection ->
        for (header in headerList) {
            val head = header.replace('-', '_')
            val cursor = statement.executeQuery("select name, translatable, ${head} from $tableName")
            val resources = AStringResource()
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

fun writeStringResourceXML(aStringResource: AStringResource, folderPath: String, filePath: String) {
    val jaxbContext = JAXBContext.newInstance(AStringResource::class.java)
    val jaxbMarshaller = jaxbContext.createMarshaller()
    try {
        val file = File(filePath)
        val folder = File(folderPath)
        folder.mkdirs()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        jaxbMarshaller.marshal(aStringResource, file)
    } catch (error: JAXBException) {
        error.printStackTrace()
    }
}

// Plurals
fun databaseToPluralXML(headerList: List<String>) {
    val tableName = "plural_translation"
    val dbName = "build/xml_translation"
    lock(dbName, tableName) { statement, connection ->
        var name: String = ""
        for (header in headerList) {
            val head = header.replace('-', '_')
            val cursor = statement.executeQuery("select name, quantity, ${head} from $tableName")
            val resources = APluralResource()
            val pluralList = ArrayList<APlural>()
            var pluralItemList = ArrayList<APluralItem>()
            var tempName: String = ""
            var index = 0
            while (cursor.next()) {
                val text = cursor.getString(cursor.findColumn(head))
                name = cursor.getString(cursor.findColumn("name"))
                val quantity = cursor.getString(cursor.findColumn("quantity"))
                val aPluralItem = APluralItem()
                aPluralItem.quantity = quantity
                aPluralItem.text = text
                pluralItemList.add(aPluralItem)
                if (name != tempName) {
                    if (index > 0) {
                        //push
                        pluralItemList.removeAt(pluralItemList.lastIndex)
                        val aPlural = APlural()
                        aPlural.aPluralItems = pluralItemList
                        aPlural.name = tempName
                        pluralList.add(aPlural)
                        pluralItemList = ArrayList<APluralItem>()
                        pluralItemList.add(aPluralItem)
                    }
                    tempName = name
                }
                index++
            }
            val aPlural = APlural()
            aPlural.aPluralItems = pluralItemList
            aPlural.name = name
            pluralList.add(aPlural)


            resources.aPluralList = pluralList
            //println(resources.toString())
            writePluralResourceXML(resources, header, "$header/plurals.xml")
        }
    }
}

fun writePluralResourceXML(aPluralResource: APluralResource, folderPath: String, filePath: String) {
    val jaxbContext = JAXBContext.newInstance(APluralResource::class.java)
    val jaxbMarshaller = jaxbContext.createMarshaller()
    try {
        val file = File(filePath)
        val folder = File(folderPath)
        folder.mkdirs()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        jaxbMarshaller.marshal(aPluralResource, file)
    } catch (error: JAXBException) {
        error.printStackTrace()
    }
}

//Arrays
fun databaseToArrayXML(headerList: List<String>) {
    val tableName = "arrays_translation"
    val dbName = "build/xml_translation"
    lock(dbName, tableName) { statement, connection ->
        var name: String = ""
        for (header in headerList) {
            val head = header.replace('-', '_')
            val cursor = statement.executeQuery("select name, ${head} from $tableName")
            val resources = AArrayResource()
            val aArrayList = ArrayList<AArray>()
            var aArrayItemList = ArrayList<AArrayItem>()
            var tempName: String = ""
            var index = 0
            while (cursor.next()) {
                val text = cursor.getString(cursor.findColumn(head))
                name = cursor.getString(cursor.findColumn("name"))
                val aArrayItem = AArrayItem()
                aArrayItem.text = text
                aArrayItemList.add(aArrayItem)
                if (name != tempName) {
                    if (index > 0) {
                        //push
                        aArrayItemList.removeAt(aArrayItemList.lastIndex)
                        val aArray = AArray()
                        aArray.aArrayItem = aArrayItemList
                        aArray.name = tempName
                        aArrayList.add(aArray)
                        aArrayItemList = ArrayList<AArrayItem>()
                        aArrayItemList.add(aArrayItem)
                    }
                    tempName = name
                }
                index++
            }
            val aArray = AArray()
            aArray.aArrayItem = aArrayItemList
            aArray.name = name
            aArrayList.add(aArray)


            resources.aArrayList = aArrayList
            //println(resources.toString())
            writeArrayResourceXML(resources, header, "$header/arrays.xml")
        }
    }
}

fun writeArrayResourceXML(aArrayResource: AArrayResource, folderPath: String, filePath: String) {
    val jaxbContext = JAXBContext.newInstance(AArrayResource::class.java)
    val jaxbMarshaller = jaxbContext.createMarshaller()
    try {
        val file = File(filePath)
        val folder = File(folderPath)
        folder.mkdirs()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        jaxbMarshaller.marshal(aArrayResource, file)
    } catch (error: JAXBException) {
        error.printStackTrace()
    }
}