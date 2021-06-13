package ru.disasm.civ.lzw;

import ru.disasm.civ.AbstractContainer;
import java.io.*;
import java.util.*;

public class LzwContainer extends AbstractContainer {

    private VgaPaletteEntry[] vgaPaletteEntries;
    private List<LzwEntry> dictionary;
    private boolean doubled;
    private int picSize;
    private int picWidth;
    private int picHeight;
    private byte lzwMaxDictBitnes;
    private int lzwCurword;
    private byte lzwBitremainOfCurword;
    private Deque<Byte> stack;
    private byte lzwCurDictBitnes;
    private int maxLzwDictionaryCode;
    private int lastCode =0;
    private byte lastCodeFirstByte;
    private int repeatCnt = 0;
    private byte repeatedByte = 0;

    LzwContainer(File inFile) throws FileNotFoundException {
        super(inFile);
    }

    private void initDictionary() {
        dictionary = new ArrayList<>(0x800);
        for (int i=0;i<0x100;i++){
            LzwEntry lzwEntry = new LzwEntry();
            lzwEntry.setPrefix(null);
            lzwEntry.setSuffix((byte) i);
            dictionary.add(lzwEntry);
        }
        lzwCurDictBitnes = 9;
        maxLzwDictionaryCode = 0x01ff;
    }

    protected void init() {
        vgaPaletteEntries = new VgaPaletteEntry[256];
        stack = new ArrayDeque<>();
        while (true) {
            byte[] cmd = new byte[2];
            try {
                secureRead(cmd);
                if (cmd[0]==0x45 && cmd[1]==0x30) {
                    //EGA palette
                    int length = readWord(cmd);
                    secureSkip(length);
                } else if (cmd[0]==0x4D && cmd[1]==0x30) {
                    //VGA palette
                    secureRead(cmd);
                    secureRead(cmd);
                    int startIndex = cmd[0] & 0xff;
                    int endIndex = cmd[1] & 0xff;
                    byte[] palEntry = new byte[3];
                    for(int i = startIndex; i<=endIndex; i++) {
                        secureRead(palEntry);
                        vgaPaletteEntries[i] = new VgaPaletteEntry(palEntry);
                    }
                } else if (cmd[0]==0x58) {
                    //Pic data
                    doubled = ((cmd[1] & 1) > 0);
                    picSize = readWord(cmd);
                    picWidth = readWord(cmd);
                    picHeight = readWord(cmd);
                    lzwCurword = readWord(cmd);
                    lzwMaxDictBitnes = cmd[0];
                    if (lzwMaxDictBitnes >11)
                        lzwMaxDictBitnes = 11;
                    lzwBitremainOfCurword = 8;
                    initDictionary();
                    stack.clear();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte readcodeFromLzw() throws IOException {
        if (stack.size()>0)
            return stack.pop();
        int wData =  lzwCurword;
        //данные могут идти блоками по 9,10 или 11 бит (хранится в lzwCurDictBitnes).
        //Чтобы получить их из 16 битных слов, приходится вот так помучиться
        wData = wData >> (0x10 - lzwBitremainOfCurword);
        while (lzwBitremainOfCurword < lzwCurDictBitnes) {
            //бит, оставшихся с текущих данных не хватает, чтобы собрать блок, считываем следующее слово
            byte[] cmd = new byte[2];
            lzwCurword = readWord(cmd);
            wData |= (lzwCurword << lzwBitremainOfCurword);
            lzwBitremainOfCurword += 0x10;
        }
        lzwBitremainOfCurword -= lzwCurDictBitnes; //те биты, что не использовались при чтении 9,10 или 11 битного блока из 16 битного слова, сохраним
        wData &= maxLzwDictionaryCode; //лишние биты слева обрежем
        int originCode = wData;
        if (wData >= dictionary.size()) {
            //Такого кода нет в словаре, эта ситуация в lzw возникает когда кодируют CsCsC при этом Cs уже закодирован, а CsC еще нет
            //поэтому данный код это CsC, то есть предыдущий код Cs плюс C - первая буква предыдущего кода
            wData = lastCode;
		    stack.push(lastCodeFirstByte);
        }
        LzwEntry wHiCode = dictionary.get(wData);
        while (wHiCode.getPrefix()!=null) {
            byte wLoCode = wHiCode.getSuffix();
            stack.push(wLoCode);
            wHiCode  = wHiCode.getPrefix();
        }
        byte wLoCode = wHiCode.getSuffix();
        stack.push(wLoCode);
        lastCodeFirstByte = wLoCode;
        LzwEntry newEntry = new LzwEntry();
        newEntry.setSuffix(wLoCode);
        if (lastCode<dictionary.size()){
            newEntry.setPrefix(dictionary.get(lastCode));
        }
        dictionary.add(newEntry);
        if (dictionary.size()> maxLzwDictionaryCode) {
            lzwCurDictBitnes++;
            maxLzwDictionaryCode = (maxLzwDictionaryCode << 1) | 1;
        }
        if ((0xff & lzwCurDictBitnes) > (0xff & lzwMaxDictBitnes)) {
            initDictionary();
        }
        lastCode = originCode;
        return stack.pop();
    }

    void lzwDecodeLine(int length, byte[] buf, int startpos) throws IOException {
        int resLen = length;
        if (doubled) {
            resLen++;
            resLen >>= 1;
        }
        for (int i=0;i<resLen;i++){
            byte code;
            if (repeatCnt==0) {
                code = readcodeFromLzw();
                if ((0xff & code) == 0x90) {
                    //специальная команда на повторение последнего символа сколько то раз
                    //если после нее идёт 0 то это не команда, а просто байт 0x90
                    code = readcodeFromLzw();
                    if ((0xff & code)>0) {
                        //обработка спецкоманды
                        code--;
                        repeatCnt = code & 0xff;
                        code = repeatedByte;
                        repeatCnt--;
                    }
                }
                repeatedByte = code;
            } else {
                code = repeatedByte;
                repeatCnt--;
            }
            if (doubled) {
                int index = i*2;
                if (index<(buf.length-startpos))
                    buf[startpos+(index++)] = (byte)(code & 0x0f);
                if (index<(buf.length-startpos))
                    buf[startpos+index] = (byte)((code & 0xf0) >>4);
            } else {
                buf[startpos+i] = code;
            }
        }
    }

    int getPicWidth() {
        return picWidth;
    }

    int getPicHeight() {
        return picHeight;
    }

    VgaPaletteEntry[] getVgaPaletteEntries() {
        return vgaPaletteEntries;
    }
}
