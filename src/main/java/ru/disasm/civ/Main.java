package ru.disasm.civ;

import ru.disasm.civ.lzw.LzwContainer;
import ru.disasm.civ.lzw.VgaPaletteEntry;
import ru.disasm.civ.png.ImageGenerator;

import java.io.ByteArrayOutputStream;
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
        String[] list = inFolder.list((f,s)->{
            int lastIndex = s.lastIndexOf('.');
            if (lastIndex<0)
                return false;
            String str = s.substring(lastIndex);
            return str.equalsIgnoreCase(".PIC");
        });
        assert list != null;
        for (String fNameStr:list){
            System.out.println("Processing file "+fNameStr);
            LzwContainer lzwContainer = new LzwContainer(new File(civPath.toAbsolutePath()+"/"+fNameStr));
            ByteArrayOutputStream bao = new ByteArrayOutputStream(lzwContainer.getPicWidth()*lzwContainer.getPicHeight()*3);
            byte[] picBuf = new byte[lzwContainer.getPicWidth()];
            byte[] emptyPal ={0,0,0};
            for (int i = 0; i<lzwContainer.getPicHeight(); i++){
                try {
                    lzwContainer.lzwDecodeLine(lzwContainer.getPicWidth(), picBuf, 0);
                    for(byte b: picBuf){
                            VgaPaletteEntry p = lzwContainer.getVgaPaletteEntries()[0xff & b];
                            if (p!=null) {
                                bao.write(p.getBytes());
                            } else
                                bao.write(emptyPal);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            lzwContainer.close();
            ImageGenerator img = new ImageGenerator();
            img.generate(new File(outPath.toAbsolutePath()+"/"+ fNameStr.substring(0,fNameStr.lastIndexOf("."))+".png"),lzwContainer.getPicWidth(),lzwContainer.getPicHeight(),bao.toByteArray());
            bao.close();
            System.out.println("Created file "+fNameStr.substring(0,fNameStr.lastIndexOf("."))+".png");
        }
    }
}
