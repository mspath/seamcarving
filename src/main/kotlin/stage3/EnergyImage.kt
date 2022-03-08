package stage3

import parseArgs
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

/*
stage 3

The first step is to calculate the energy for each pixel of the image. Energy is the pixel's importance.
The higher the pixel energy, the less likely this pixel is to be removed from the picture while reducing.
There are several different energy functions invented for seam carving. In this project, we will use
dual-gradient energy function.
see https://hyperskill.org/projects/100/stages/552/implement

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

fun BufferedImage.applyIntensity(): BufferedImage {
    val energyMax = maxEnergy()
    val intenseImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    (0 until width).forEach { x ->
        (0 until height). forEach { y ->
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
            val intensity = (255.0 * energy / energyMax).toInt()
            intenseImage.setRGB(x, y, Color(intensity, intensity, intensity).rgb)
        }
    }
    return intenseImage
}

fun main(args: Array<String>) {
    val files = parseArgs(args)
    val inputFile = File(files.first)
    val outputFile = File(files.second)

    val myImage: BufferedImage = ImageIO.read(inputFile)
    val intenseImage = myImage.applyIntensity()
    ImageIO.write(intenseImage, "png", outputFile)
}