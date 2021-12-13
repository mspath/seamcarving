package stage1

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/*
 stage 1: draw 2 diagonal lines
 */

fun main() {
    println("Enter rectangle width:")
    val width = readLine()!!.toInt()
    println("Enter rectangle height:")
    val height = readLine()!!.toInt()
    println("Enter output image name:")
    val out = readLine()!!
    val imageFile = File(out)

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    graphics.color = Color.BLACK
    graphics.drawRect(0, 0, width - 1, height - 1)
    graphics.color = Color.RED
    graphics.drawLine(0, 0, width - 1, height - 1)
    graphics.drawLine(0, height - 1, width - 1, 0)
    ImageIO.write(image, "png", imageFile)
}