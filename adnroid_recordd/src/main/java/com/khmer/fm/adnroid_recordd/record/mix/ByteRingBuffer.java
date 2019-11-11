package com.khmer.fm.adnroid_recordd.record.mix;

// Copyright 2014 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
//  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.

/**
 * A ring buffer (circular buffer, FIFO) for bytes.
 * <p>
 * <p>All methods of this class are non-blocking and not synchronized (not thread-safe).
 */

/**
 * 字节循环缓存器
 */
public class ByteRingBuffer {

    private byte[] rBuf;                      // ring buffer data 缓存的数据
    private int rBufSize;                     // ring buffer size 缓存的大小
    private int rBufPos;                      // position of first (oldest) data byte within the ring buffer 缓存的位置
    private int rBufUsed;                     // number of used data bytes within the ring buffer 已经使用的缓存的位置

    /**
     * Creates a ring buffer.
     * 创建一个缓存区
     */
    public ByteRingBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        rBufSize = size;
        rBuf = new byte[rBufSize];
    }

    /**
     * Resizes the ring buffer by preserving it's data.
     * 创建新大小的的缓存区
     * <p>
     * <p>If the new size is not enough to keep all used data in the ring buffer,
     * the excess old data is discarded.
     */
    public void resize(int newSize) {
        if (newSize <= 0) {
            throw new IllegalArgumentException();
        }
        if (newSize < rBufUsed) {                               // if new buffer is too small to contain all data
            discard(rBufUsed - newSize);
        }                       // discard oldest data
        byte[] newBuf = new byte[newSize];
        int newBufUsed = read(newBuf, 0, newSize);              // transfer data to new buffer
        rBuf = newBuf;
        rBufSize = newSize;
        rBufPos = 0;
        rBufUsed = newBufUsed;
    }

    /**
     * Returns the size of the ring buffer.
     */
    public int getSize() {
        return rBufSize;
    }

    /**
     * Returns the number of free bytes within the ring buffer.
     * 返回空闲字节
     */
    public int getFree() {
        return rBufSize - rBufUsed;
    }

    /**
     * Returns the number of used bytes within the ring buffer.
     * 返回正在使用的位置
     */
    public int getUsed() {
        return rBufUsed;
    }

    /**
     * Clears the ring buffer.
     * 清空使用的字节
     */
    public void clear() {
        rBufPos = 0;
        rBufUsed = 0;
    }

    /**
     * Discards the oldest <code>len</code> bytes within the ring buffer.
     * This has the same effect as calling <code>read(new byte[len], 0, len)</code>.
     *  放弃len个字节
     * @param len The number of bytes to be discarded.
     */
    public void discard(int len) {
        if (len < 0) {
            throw new IllegalArgumentException();
        }
        int trLen = Math.min(len, rBufUsed);
        rBufPos = clip(rBufPos + trLen);
        rBufUsed -= trLen;
    }

    /**
     * Writes data to the ring buffer.
     *
     * @return The number of bytes written.
     * This is guaranteed to be <code>min(len, getFree())</code>.
     */
    public int write(byte[] buf, int pos, int len) {
        if (len < 0) {
            throw new IllegalArgumentException();
        }
        if (rBufUsed == 0) {
            rBufPos = 0;
        }                                       // (speed optimization)
        int p1 = rBufPos + rBufUsed;
        if (p1 < rBufSize) {                                    // free space in two pieces
            int trLen1 = Math.min(len, rBufSize - p1);
            append(buf, pos, trLen1);
            int trLen2 = Math.min(len - trLen1, rBufPos);
            append(buf, pos + trLen1, trLen2);
            return trLen1 + trLen2;
        } else {                                                 // free space in one piece
            int trLen = Math.min(len, rBufSize - rBufUsed);
            append(buf, pos, trLen);
            return trLen;
        }
    }

    /**
     * Writes data to the ring buffer.
     * <p>
     * <p>Convenience method for: <code>write(buf, 0, buf.length)</code>
     */
    public int write(byte[] buf) {
        return write(buf, 0, buf.length);
    }

    private void append(byte[] buf, int pos, int len) {
        if (len == 0) {
            return;
        }
        if (len < 0) {
            throw new AssertionError();
        }
        int p = clip(rBufPos + rBufUsed);
        System.arraycopy(buf, pos, rBuf, p, len);
        rBufUsed += len;
    }

    /**
     * Reads data from the ring buffer.
     *
     * @return The number of bytes read.
     * This is guaranteed to be <code>min(len, getUsed())</code>.
     */
    public int read(byte[] buf, int pos, int len) {
        if (len < 0) {
            throw new IllegalArgumentException();
        }
        int trLen1 = Math.min(len, Math.min(rBufUsed, rBufSize - rBufPos));
        remove(buf, pos, trLen1);
        int trLen2 = Math.min(len - trLen1, rBufUsed);
        remove(buf, pos + trLen1, trLen2);
        return trLen1 + trLen2;
    }

    /**
     * Reads data from the ring buffer.
     * <p>
     * <p>Convenience method for: <code>read(buf, 0, buf.length)</code>
     */
    public int read(byte[] buf) {
        return read(buf, 0, buf.length);
    }

    private void remove(byte[] buf, int pos, int len) {
        if (len == 0) {
            return;
        }
        if (len < 0) {
            throw new AssertionError();
        }
        System.arraycopy(rBuf, rBufPos, buf, pos, len);
        rBufPos = clip(rBufPos + len);
        rBufUsed -= len;
    }

    private int clip(int p) {
        return (p < rBufSize) ? p : (p - rBufSize);
    }

}
