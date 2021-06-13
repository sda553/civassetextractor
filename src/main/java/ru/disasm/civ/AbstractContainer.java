package ru.disasm.civ;

import java.io.*;

public abstract class AbstractContainer  implements Closeable {
    private final FileInputStream fin;

    protected AbstractContainer(File inFile) throws FileNotFoundException {
        fin = new FileInputStream(inFile);
        init();
    }

    protected void secureRead(byte[] buf) throws IOException {
        int res = fin.read(buf);
        if (res==0) {
            throw new IOException("Unexpected end of file");
        }
    }

    protected int readWord(byte[] buf) throws IOException {
        secureRead(buf);
        return (buf[0] & 0xff) + ((buf[1]  << 8) & 0xff00);
    }

    protected void secureSkip(int len) throws IOException {
        if (fin.skip(len)!=len) {
            throw new IOException("Unexpected end of file");
        }
    }

    protected abstract void init();

    @Override
    public void close() {
        try {
            if (fin != null) {
                fin.close();
            }
        }
        catch (IOException ioe) {
            System.err.println("Error while closing stream: " + ioe);
        }
    }
}
