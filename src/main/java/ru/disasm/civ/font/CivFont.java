package ru.disasm.civ.font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class CivFont {
    int position;
    byte[] fontSprite;
    byte height;
    byte firstChar;
    byte lastChar;
    byte charLineSize;
    byte fontFixedWidth;
    byte fontIntevalX;
    byte[] charWidths;
    int fontsetLineSize;

    int calculateSpriteWidth() {
        if (fontFixedWidth>0)
            return (0xff & fontFixedWidth)* (0xff & (lastChar-firstChar+1));
        int symbolCount = 0xff & (lastChar-firstChar+1);
        int resultWidth = 0;
        for (int i=0;i<symbolCount-1;i++){
            resultWidth+=charWidths[i]+fontIntevalX;
        }
        resultWidth+=charWidths[symbolCount-1];
        return resultWidth;
    }

    void render(ByteArrayOutputStream bao, int renderWidth) throws IOException {
        int symbolCount = 0xff & (lastChar-firstChar+1);
        int pos;
        byte[] blackPal ={0,0,0};
        byte[] whitePal ={-1,-1,-1};
        for (int y=0;y<height;y++){
            int x=0;
            for (int i=0;i<symbolCount;i++){
                pos = i*charLineSize+y*fontsetLineSize;
                int innerpos = 0;
                int symbolwidth = 0xff & fontFixedWidth;
                if (symbolwidth==0) {
                    symbolwidth = charWidths[i]+fontIntevalX;
                }
                int renderedWidth = symbolwidth-fontIntevalX;
                int locX = 0;
                while (locX<renderedWidth && x<renderWidth){
                    int bit = ((0xff & fontSprite[pos])<<(innerpos+1)) & 0x100;
                    if (bit!=0){
                        bao.write(blackPal);
                    } else
                        bao.write(whitePal);
                    innerpos++;
                    if (innerpos>7){
                        innerpos = 0;
                        pos++;
                    }
                    x++;
                    locX++;
                }
                while (locX<symbolwidth && x<renderWidth){
                    bao.write(whitePal);
                    locX++;
                    x++;
                }
            }
            while (x<renderWidth){
                bao.write(whitePal);
                x++;
            }
        }
    }
}
