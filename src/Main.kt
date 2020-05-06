import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception

fun main() {
    val deviceList = mutableListOf<List<String>>()
    val csvReader = BufferedReader(FileReader("arcore_devicelist.csv"))

    var row: String = ""
    while (csvReader.readLine()?.also { row = it } != null) {
        val data = row.split(",")
        // do something with the data
        if (data[4] == "Phone") {
            if (data[9].contains(";")) {
                val versionsString = data[9].split(";")
                val versions = versionsString.map { it.toInt() }
                for(version in versions) {
                    if (version >= 26) {
                        deviceList.add(data)
                        break
                    }
                }
            } else {
                val version = data[9].toInt()
                if (version >= 26) {
                    deviceList.add(data)
                }
            }
        }
    }
    csvReader.close()

    val phoneList = mutableListOf<Phone>()

    val phones = deviceList.map {
        Pair("${it[0]} ${it[1]}", it[6])
    }.map {
        val resolutionString = it.second

        var aspectRatio: String = ""
        if (resolutionString.contains(";")) {
            val supportedResolutions = resolutionString.split(";")

            val ratios = mutableListOf<String>()
            supportedResolutions.forEach {
                val resolution = splitResolution(it)
                ratios.add(getAspectRatio(resolution))
            }

            aspectRatio = ratios.joinToString(separator = ";")
        }else {
            val resolution = splitResolution(resolutionString)
            aspectRatio = getAspectRatio(resolution)
        }

        Phone(it.first, aspectRatio, resolutionString)
    }

    phones.forEach {
        println(it)
    }

    try {

        val myFile = File("exported.csv")
        myFile.createNewFile()

        val writer = FileWriter(myFile)

        for (phone in phones) {
            writer.append("${phone.name}, ${phone.aspect}, ${phone.resolution}\n")
        }

        writer.close()
    }catch (e: Exception) {

    }

}

fun getAspectRatio(resolution: Pair<Int, Int>): String {
    val hcf = getHcf(resolution)

    return "${resolution.second/hcf}:${resolution.first/hcf}"
}

fun getHcf(resolution: Pair<Int, Int>): Int {
    var m = resolution.first
    var n = resolution.second

    var temp: Int
    var reminder: Int
    if (m < n) {
        temp = m
        m = n
        n = temp
    }

    while (true) {
        reminder = m % n
        if (reminder == 0) {
            return n
        }else {
            m = n
        }

        n = reminder
    }
}

fun splitResolution(resolution: String): Pair<Int, Int> {
    val pair = resolution.split("x")
    return Pair(pair.first().toInt(), pair.last().toInt())
}

data class Phone(
    val name: String,
    val aspect: String,
    val resolution: String
)