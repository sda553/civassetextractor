package ru.disasm.civ;

import ru.disasm.civ.font.FontReader;
import ru.disasm.civ.lzw.LzwReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {
        String civPathStr;
        String outPathStr = null;
        if (args.length==0) {
            System.err.println("Usage: [-out path] path");
            return;
        }
        if (args.length == 1)
            civPathStr = args[0];
        else if (args.length!=3) {
            System.err.println("Usage: [-out path] path");
            return;
        }
        else if (!args[0].equals("-out")) {
            System.err.println("Usage: [-out path] path");
            return;
        }
        else {
            civPathStr = args[2];
            outPathStr = args[1];
        }
        Path civPath = Paths.get(civPathStr);
        Path outPath = outPathStr==null ? Paths.get("") : Paths.get(outPathStr);
        System.out.println("Input civilization path = " + civPath.toAbsolutePath());
        System.out.println("Output assets path = " + outPath.toAbsolutePath());
        File inFolder = civPath.toAbsolutePath().toFile();
        LzwReader.readLzw(inFolder,outPath);
        FontReader.readFonts(inFolder,outPath);
    }
}
