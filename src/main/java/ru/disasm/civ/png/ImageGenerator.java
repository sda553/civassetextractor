package ru.disasm.civ.png;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class ImageGenerator {
    private final ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

    public void generate(File outFile, int width, int height, byte[] aByteArray) {
        DataBuffer buffer = new DataBufferByte(aByteArray, aByteArray.length);
        WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 3 * width, 3, new int[] {0, 1, 2}, null);
        BufferedImage image = new BufferedImage(cm, raster, true, null);
        try {
            ImageIO.write(image, "png", outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
