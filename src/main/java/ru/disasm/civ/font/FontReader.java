package ru.disasm.civ.font;

import ru.disasm.civ.png.ImageGenerator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FontReader {
    private static final String fontFileName = "FONTS.CV";

    public static void readFonts(File inFolder, Path outPath) throws IOException {
        File fontFile = new File(inFolder.getAbsolutePath()+"/"+fontFileName);
        if (!fontFile.exists()){
            System.out.println("Font file "+fontFileName+" not found in path! Unable to extract fonts.");
            return;
        }
        System.out.println("Processing file "+fontFileName);
        FontContainer fc = new FontContainer(fontFile);
        ByteArrayOutputStream bao = new ByteArrayOutputStream(fc.renderWidth*fc.renderHeight*3);
        fc.render(bao);
        ImageGenerator img = new ImageGenerator();
        img.generate(new File(outPath.toAbsolutePath()+"/"+ "fonts.png"),fc.renderWidth,fc.renderHeight,bao.toByteArray());
        bao.close();
        System.out.println("Created file "+outPath.toAbsolutePath()+"/"+ "fonts.png");
        fc.close();
    }
}
