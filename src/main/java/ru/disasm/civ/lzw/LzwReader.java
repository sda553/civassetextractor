package ru.disasm.civ.lzw;

import ru.disasm.civ.png.ImageGenerator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LzwReader {
    public static void readLzw(File inFolder, Path outPath) throws IOException {
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
            LzwContainer lzwContainer = new LzwContainer(new File( inFolder.getAbsolutePath()+"/"+fNameStr));
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
