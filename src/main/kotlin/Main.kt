fun parseArgs(args: Array<String>): Pair<String, String> {
    var fileIn = ""
    var fileOut = ""
    if (args.size > 1 && args[0] == "-in") {
        fileIn = args[1]
    } else if (args.size > 3 && args[2] == "-in") {
        fileIn = args[3]
    }
    if (args.size > 1 && args[0] == "-out") {
        fileOut = args[1]
    } else if (args.size > 3 && args[2] == "-out") {
        fileOut = args[3]
    }
    return Pair(fileIn, fileOut)
}

fun main(args: Array<String>) {
    stage6.main(args)
}