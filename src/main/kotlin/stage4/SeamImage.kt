package stage4

import parseArgs
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.Comparator
import kotlin.math.pow
import kotlin.math.sqrt

/*
stage 4

Now you are ready to find the best seam to remove. Vertical seam is a sequence of adjacent pixels
crossing the image top to bottom. The seam can have only one pixel in each row of the image.
For example, subsequent pixels for pixel (x,y)(x,y)(x,y) are (x−1,y+1)(x -1, y + 1)(x−1,y+1),
(x,y+1)(x, y + 1)(x,y+1) , and (x+1,y+1)(x + 1, y + 1)(x+1,y+1).
The best seam to remove is the seam with the lowest sum of pixel energies from all possible seams.
 The problem of finding the best seam is very similar to finding the shortest path in a graph.
 Think of pixels as vertices. Connect them with imaginary edges. Edge weight should be equal to
 the energy of the pixel this edge is pointing to.
The easiest way to apply the shortest path finding algorithm to your energy graph is to add imaginary
 zero rows with the horizontal links on the top and on the bottom. Then you can search for the
 shortest path between top-left and bottom-right corners.
see https://hyperskill.org/projects/100/stages/553/preview

Don't use normalized energies (Int) from previous stage. Use source energies (Double).
Otherwise you will get a slightly different seam for the blue / sky image
*/


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

    var originalImage: BufferedImage = ImageIO.read(File("test.png"))
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

    fun applySeam() {
        // solve() needs to be called first
        val path: MutableList<Int> = mutableListOf(cells.last().previous)
        var previous = cells.last().previous
        while (previous > 0) {
            val parent = Seam.cells[previous].previous
            path.add(0, parent)
            previous = parent
        }
        path.removeAll {
            it <= Seam.width || it >= cells.size - Seam.width
        }

        path.map {
            it - width
        }.forEach {
            val (x, y) = toCoordinates(it)
            originalImage.setRGB(x, y, Color(255, 0, 0).rgb)
        }
    }
}

fun main(args: Array<String>) {
    val files = parseArgs(args)
    val inputFile = File(files.first)
    val outputFile = File(files.second)
    val myImage: BufferedImage = ImageIO.read(inputFile)
    Seam.originalImage = ImageIO.read(inputFile)

    val energies = myImage.applySourceIntensity()
    val matrix: MutableList<Double> = mutableListOf()
        (0 until myImage.width).forEach {
        matrix.add(0.0)
    }
    matrix.addAll(energies)
    (0 until myImage.width).forEach {
        matrix.add(0.0)
    }

    Seam.cells = matrix.mapIndexed { index, c ->
        if (index == 0) Cell(index, 0.0, 0.0)
        else Cell(index, c, Double.MAX_VALUE)
    }.toMutableList()

    Seam.solve()
    Seam.applySeam()


    ImageIO.write(Seam.originalImage, "png", outputFile)
}