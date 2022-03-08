package stage2

import parseArgs
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/*
stage 2

Before digital photography came around, people used to work with negative images on film.
Let’s get nostalgic and emulate a warm analog negative film image!
To create a negative image, you should invert all color components for every pixel.
Inverted color for (r, g, b) is (255 - r, 255 - g, 255 - b).
Objective
At this stage, you should add command-line parameters to specify the input and output files for your program.
Use parameter -in for the input file path and parameter -out for the output file path.
For simplicity, we will use only .png file format in this project. Your program should read the
specified input image file, inverse colors and save the file to a given location.
 */

fun main(args: Array<String>) {
    val files = parseArgs(args)
    val inputFile = File(files.first)
    val outputFile = File(files.second)

    val myImage: BufferedImage = ImageIO.read(inputFile)
    for (x in 0 until myImage.width) {
        for (y in 0 until myImage.height) {
            val color = Color(myImage.getRGB(x, y))
            val colorNew = Color(255 - color.red, 255 - color.green, 255 - color.blue)
            myImage.setRGB(x, y, colorNew.rgb)
        }
    }
    ImageIO.write(myImage, "png", outputFile)
}