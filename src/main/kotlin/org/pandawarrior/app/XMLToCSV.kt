package org.pandawarrior.app

import java.io.File
import java.io.StringReader
import java.nio.file.Paths
import javax.xml.bind.JAXBContext

/**
 * Created by jtlie on 3/31/2017.
 */

fun getHeadersFromDirectory(): ArrayList<String> {
    val currentPath = Paths.get(".").toAbsolutePath().normalize().toString()
    val folder = File(currentPath)
    val lists = ArrayList<String>()
    for (file in folder.listFiles()) {
        if (file.isDirectory && file.name.contains("value")) {
            lists.add(file.name)
        }
    }
    return lists
}

fun xmlToDatabase(dbName: String, headers: ArrayList<String>, fileName: String) {
    lock(dbName) { statement ->
        val createHeaderString = headers.joinToString { "${it} string" }.replace('-', '_')
        statement.executeUpdate("drop table if exists `translation`")
        statement.executeUpdate("create table `translation` (name string, translatable string, ${createHeaderString})")
        for (header in headers) {
            val file = File("$header${File.separator}$fileName")
            val jaxbContext = JAXBContext.newInstance(AResounce::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val aResources = jaxbUnmarchaller.unmarshal(file) as AResounce
            if (aResources.aStringList == null) {
                throw Exception("Parsing Error")
            }
            var aStringList: List<AString> = aResources.aStringList!!
            for (aString in aStringList) {
                val updateCount = statement.executeUpdate("UPDATE `translation` SET ${header.replace('-', '_')} = '${aString.text}' WHERE name='${aString.name}'")
                if (updateCount < 1) {
                    statement.executeUpdate("INSERT INTO `translation` (name, translatable, $header) VALUES('${aString.name}', ${aString.translatable}, '${aString.text}')")
                }
            }
        }
    }
}