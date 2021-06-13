package ru.disasm.civ.font;

import ru.disasm.civ.AbstractContainer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class FontContainer extends AbstractContainer {
    private CivFont[] fontList;
    private int position;
    int renderWidth;
    int renderHeight;

    FontContainer(File inFile) throws FileNotFoundException {
        super(inFile);
    }

    @Override
    protected int readWord(byte[] buf) {
        int result = (buf[position] & 0xff) + ((buf[position+1]  << 8) & 0xff00);
        position+=2;
        return result;
    }

    private byte readByte(byte[] buf) {
        return (byte)(buf[position++] & 0xff);
    }

    @Override
    protected void init() {
        byte[] buffer = new byte[0x8000];
        try {
            secureRead(buffer);
            super.close();
            position = 0;
            int fontNumber = readWord(buffer);
            fontList = new CivFont[fontNumber];
            for (int i = 1; i<= fontNumber; i++) {
                fontList[i-1] = new CivFont();
                fontList[i-1].position = readWord(buffer);
            }
            renderWidth = 0;
            renderHeight = 0;
            for(CivFont font: fontList){
                position = font.position-8;
                font.firstChar = readByte(buffer);
                font.lastChar = readByte(buffer);
                byte symLineSizeln = readByte(buffer);
                font.charLineSize = (byte)(1 << (symLineSizeln-1));
                font.fontFixedWidth = readByte(buffer);
                font.height = readByte(buffer);
                font.fontIntevalX = readByte(buffer);
                if (font.fontFixedWidth >0) {
                    font.fontFixedWidth +=font.fontIntevalX;
                }
                if (font.fontFixedWidth==0){
                    int symbolCount = 0xff & (font.lastChar-font.firstChar+1);
                    position = font.position-8-symbolCount;
                    font.charWidths = Arrays.copyOfRange(buffer,position,position+symbolCount);
                }
                font.fontsetLineSize = font.charWidths.length*font.charLineSize;
                font.fontSprite = Arrays.copyOfRange(buffer,font.position,font.position+font.fontsetLineSize*font.height);
                int allFontWidth = font.calculateSpriteWidth();
                if (allFontWidth>renderWidth)
                    renderWidth =allFontWidth;
                renderHeight+=font.height;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void render(ByteArrayOutputStream bao) throws IOException {
        for (CivFont font: fontList){
            font.render(bao, renderWidth);
        }
    }

    @Override
    public void close() {
    }
}
