package com.alibaba.polardbx.common.oss.filesystem;

import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWithRateLimiter extends InputStream implements Seekable, PositionedReadable {
    final private FileSystemRateLimiter rateLimiter;
    final private InputStream in;

    public InputStreamWithRateLimiter(InputStream in, FileSystemRateLimiter rateLimiter) {
        if (!(in instanceof Seekable) || !(in instanceof PositionedReadable)) {
            throw new IllegalArgumentException(
                "In is not an instance of Seekable or PositionedReadable");
        }
        this.in = in;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public synchronized int read() throws IOException {
        rateLimiter.acquireRead(1);
        return in.read();
    }

    @Override
    public synchronized int read(byte @NotNull [] buf, int off, int len) throws IOException {
        rateLimiter.acquireRead(len);
        return in.read(buf, off, len);
    }

    @Override
    public synchronized int read(byte @NotNull [] b) throws IOException {
        rateLimiter.acquireRead(b.length);
        return in.read(b);
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        // TODO(siyun): should this be tracked by rate limiter?
        return in.skip(n);
    }

    @Override
    public synchronized int available() throws IOException {
        return in.available();
    }

    @Override
    public synchronized void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public synchronized boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        rateLimiter.acquireRead(length);
        return ((PositionedReadable) in).read(position, buffer, offset, length);
    }

    @Override
    public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
        rateLimiter.acquireRead(length);
        ((PositionedReadable) in).readFully(position, buffer, offset, length);
    }

    @Override
    public void readFully(long position, byte[] buffer) throws IOException {
        rateLimiter.acquireRead(buffer.length);
        ((PositionedReadable) in).readFully(position, buffer);
    }

    @Override
    public void seek(long pos) throws IOException {
        ((Seekable) in).seek(pos);
    }

    @Override
    public long getPos() throws IOException {
        return ((Seekable) in).getPos();
    }

    @Override
    public boolean seekToNewSource(long targetPos) throws IOException {
        return ((Seekable) in).seekToNewSource(targetPos);
    }
}
