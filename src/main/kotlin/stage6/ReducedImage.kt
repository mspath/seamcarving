package stage6

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.Comparator
import kotlin.math.pow
import kotlin.math.sqrt

/*
stage 6: Description
Now you have everything to resize the image while preserving its content.
Simply remove the seam, then remove the seam from the resulting image and so on!
Objective
Add two more command line parameters. Use parameter -width for the number of vertical seams to remove
and -height for horizontal seams.
At this stage, your program should reduce the input image and save the result using the following algorithm:
Find a vertical seam and remove all the pixels that this seam contains.
Then find another vertical seam on the resulted image and delete all the pixels that the second seam contains.
Repeat the process until you remove the specified number of vertical seams.
Do the same, but with horizontal seams.
 */

// creates a transposed image
fun BufferedImage.transposeImage(): BufferedImage {
    val transposedImage = BufferedImage(this.height, this.width, BufferedImage.TYPE_INT_RGB)
    (0 until this.height).forEach { y ->
        (0 until this.width).forEach { x ->
            transposedImage.setRGB(y, x, this.getRGB(x, y))
        }
    }
    return transposedImage
}

// calculates the maxenergy
fun BufferedImage.maxEnergy(): Double {
    var maxEnergy = 0.0
    (0 until width).forEach { x ->
        (0 until height).forEach { y ->
            var xW = x - 1
            var xE = x + 1
            var yN = y - 1
            var yS = y + 1
            if (x == 0) {
                xW += 1
                xE += 1
            }
            if (x == width - 1)  {
                xW -= 1
                xE -= 1
            }
            if (y == 0) {
                yN += 1
                yS += 1
            }
            if (y == height - 1) {
                yN -= 1
                yS -= 1
            }
            val colorW = Color(getRGB(xW, y))
            val colorE = Color(getRGB(xE, y))
            val colorN = Color(getRGB(x, yN))
            val colorS = Color(getRGB(x, yS))
            val xDiff2 = (colorW.red - colorE.red).toDouble().pow(2) +
                    (colorW.green - colorE.green).toDouble().pow(2) +
                    (colorW.blue - colorE.blue).toDouble().pow(2)
            val yDiff2 = (colorN.red - colorS.red).toDouble().pow(2) +
                    (colorN.green - colorS.green).toDouble().pow(2) +
                    (colorN.blue - colorS.blue).toDouble().pow(2)
            val energy = sqrt(xDiff2 + yDiff2)
            maxEnergy = maxOf(maxEnergy, energy)
        }
    }
    return maxEnergy
}

// builds a list of the source intensities of each pixel
fun BufferedImage.applySourceIntensity(): List<Double> {
    val energyMax = maxEnergy()
    val energies: MutableList<Double> = mutableListOf()
    (0 until height).forEach { y ->
        (0 until width). forEach { x ->
            var xW = x - 1
            var xE = x + 1
            var yN = y - 1
            var yS = y + 1
            if (x == 0) {
                xW += 1
                xE += 1
            }
            if (x == width - 1)  {
                xW -= 1
                xE -= 1
            }
            if (y == 0) {
                yN += 1
                yS += 1
            }
            if (y == height - 1) {
                yN -= 1
                yS -= 1
            }
            val colorW = Color(getRGB(xW, y))
            val colorE = Color(getRGB(xE, y))
            val colorN = Color(getRGB(x, yN))
            val colorS = Color(getRGB(x, yS))
            val xDiff2 = (colorW.red - colorE.red).toDouble().pow(2) +
                    (colorW.green - colorE.green).toDouble().pow(2) +
                    (colorW.blue - colorE.blue).toDouble().pow(2)
            val yDiff2 = (colorN.red - colorS.red).toDouble().pow(2) +
                    (colorN.green - colorS.green).toDouble().pow(2) +
                    (colorN.blue - colorS.blue).toDouble().pow(2)
            val energy = sqrt(xDiff2 + yDiff2)
            val intensity = (255.0 * energy / energyMax)
            energies.add(intensity)
        }
    }
    return energies
}

fun BufferedImage.removeSeam(seam: List<Int>): BufferedImage {

    fun getSeamForRow(row: Int): Int {
        return seam[row] % this.width
    }

    val reducedImage = BufferedImage(this.width - 1, this.height, BufferedImage.TYPE_INT_RGB)
    (0 until this.height).forEach { y ->
        val drop = getSeamForRow(y)
        var offsetX = 0
        (0 until this.width).forEach { x ->
            if (x == drop) {
                offsetX = 1
            } else {
                reducedImage.setRGB(x - offsetX, y, this.getRGB(x, y))
            }
        }
    }
    return reducedImage
}

data class Cell(val index: Int, val intensity: Double, var cost: Double = Double.MAX_VALUE, var previous: Int = -1) {

    var jobDone = false

    fun getNeighbors(): List<Cell> {
        val neighbors = getIndicesOfNeighbors().map {
            Seam.cells[it]
        }
        return neighbors
    }

    fun acceptVisit(visitor: Cell) {
        if (visitor.cost + intensity < cost) {
            cost = visitor.cost + intensity
            previous = visitor.index
        }
    }

    fun getIndicesOfNeighbors(): List<Int> {
        val size = Seam.cells.size
        val cols = Seam.width

        val e = index + 1
        val sw = index + cols - 1
        val s = index + cols
        val se = index + cols + 1

        // outside
        if (index !in Seam.cells.indices) return listOf()
        // last element
        if (index == Seam.cells.indices.last) return listOf()
        // last row
        if (index >= size - cols) return listOf(e)

        val neighbors = mutableListOf(sw, s, se)

        // first row add east
        if (index < cols - 1) neighbors.add(e)
        // left column remove west
        if (index % cols == 0) neighbors.remove(sw)
        // right column remove east
        if (index % cols == cols - 1) neighbors.remove(se)

        return neighbors
    }
}

object Seam {
    var originalImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    val width: Int
        get() = originalImage.width
    val height: Int
        get() = originalImage.height

    // we want a matrix with cells containing the intensities.
    var cells: MutableList<Cell> = mutableListOf()

    fun toCoordinates(i: Int): Pair<Int, Int> {
        return Pair(i % width, i / width)
    }

    // this hopefully finds the shortest path
    fun solve() {
        val byCostComparator: Comparator<Cell> = compareBy { it.cost }
        val queue = PriorityQueue<Cell>(byCostComparator)
        queue.add(cells.first())
        do {
            val next = queue.remove()
            val neighbors = next.getNeighbors().filterNot { it.jobDone }
            neighbors.forEach {
                it.acceptVisit(next)
                if (queue.contains(it)) {
                    queue.remove(it)
                }
                queue.add(it)
            }
            next.jobDone = true
        } while (queue.isNotEmpty())
    }

    fun getSeam(): List<Int> {
        val energies = originalImage.applySourceIntensity()
        val matrix: MutableList<Double> = mutableListOf()
        (0 until originalImage.width).forEach {
            matrix.add(0.0)
        }
        matrix.addAll(energies)
        (0 until originalImage.width).forEach {
            matrix.add(0.0)
        }
        cells = matrix.mapIndexed { index, c ->
            if (index == 0) Cell(index, 0.0, 0.0)
            else Cell(index, c, Double.MAX_VALUE)
        }.toMutableList()
        solve()
        val path: MutableList<Int> = mutableListOf(cells.last().previous)
        var previous = cells.last().previous
        while (previous > 0) {
            val parent = cells[previous].previous
            path.add(0, parent)
            previous = parent
        }
        path.removeAll {
            it <= width || it >= cells.size - width
        }
        return path.map {
            it - width
        }
    }
}

fun main(args: Array<String>) {
    val inputFile = File(args[1])
    val outputFile = File(args[3])
    val rowsToReduce = File(args[5]).toString().toInt()
    val colsToReduce = File(args[7]).toString().toInt()

    val myImage: BufferedImage = ImageIO.read(inputFile)
    Seam.originalImage = myImage

    for (i in 1..rowsToReduce) {
        val seam = Seam.getSeam()
        Seam.originalImage = Seam.originalImage.removeSeam(seam)
    }
    Seam.originalImage = Seam.originalImage.transposeImage()
    for (i in 1..colsToReduce) {
        val seam = Seam.getSeam()
        Seam.originalImage = Seam.originalImage.removeSeam(seam)
    }
    Seam.originalImage = Seam.originalImage.transposeImage()
    ImageIO.write(Seam.originalImage, "png", File(outputFile.toString()))
}