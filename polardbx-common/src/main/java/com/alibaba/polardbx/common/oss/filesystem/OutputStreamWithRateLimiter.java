package com.alibaba.polardbx.common.oss.filesystem;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWithRateLimiter extends OutputStream {
    final private FileSystemRateLimiter rateLimiter;
    final private OutputStream out;

    public OutputStreamWithRateLimiter(OutputStream out, FileSystemRateLimiter rateLimiter) {
        this.out = out;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public synchronized void write(byte @NotNull [] b) throws IOException {
        rateLimiter.acquireWrite(b.length);
        out.write(b);
    }

    @Override
    public synchronized void write(byte @NotNull [] b, int off, int len) throws IOException {
        rateLimiter.acquireWrite(len);
        out.write(b, off, len);
    }

    @Override
    public synchronized void flush() throws IOException {
        out.flush();
    }

    @Override
    public synchronized void close() throws IOException {
        out.close();
    }

    @Override
    public synchronized void write(int b) throws IOException {
        rateLimiter.acquireWrite(1);
        out.write(b);
    }
}
