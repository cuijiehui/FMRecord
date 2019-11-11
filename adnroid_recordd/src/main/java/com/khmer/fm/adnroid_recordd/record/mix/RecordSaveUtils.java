package com.khmer.fm.adnroid_recordd.record.mix;

import android.media.AudioFormat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RecordSaveUtils {
    private RandomAccessFile mRandomFile;
    private String mFilePath;

    /**
     * 初始化数据
     *
     * @param outputPath 文件路径
     */
    public void createNewFile(String outputPath) {
        mFilePath = outputPath;
        File mFile = new File(mFilePath);
        if (mFile.exists()) {
            mFile.delete();
        }
        try {
            mRandomFile = new RandomAccessFile(mFilePath, "rw");
            //todo 添加wav的头部
            writeWavHeader(mRandomFile, AudioFormat.CHANNEL_IN_STEREO, MixParams.SampleRateOptions.SAMPLERATE_44100,
                    AudioFormat.ENCODING_PCM_16BIT);
        } catch (IOException e) {
            // TODO: 4/9/17 handle this
            e.printStackTrace();
        }
    }

    /**
     * 把音频数据写入
     *
     * @param data
     */
    public void onDataReady(byte[] data,int size) {
        try {
            mRandomFile.write(data, 0, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void resetInfo(){
        try {
            mRandomFile = new RandomAccessFile(mFilePath, "rw");
            // 文件长度，字节数
            long fileLength = mRandomFile.length();
            // 将写文件指针移到文件尾。
            mRandomFile.seek(fileLength);
        } catch (IOException e) {
            // TODO: 4/9/17 handle this
            e.printStackTrace();
        }

    }
    public void onRecordingStopped() {
        try {
            if (mRandomFile!=null) {
                mRandomFile.close();
                mRandomFile=null;
                updateWavHeader(new File(mFilePath));
            }
            System.out.println("Record Complete. Saving and closing");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Updates the given wav file's header to include the final chunk sizes
     *
     * @param wav The wav file to update
     * @throws IOException
     */
    private void updateWavHeader(File wav) throws IOException {
        byte[] sizes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
                // There are probably a bunch of different/better ways to calculate
                // these two given your circumstances. Cast should be safe since if the WAV is
                // > 4 GB we've already made a terrible mistake.
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Subchunk2Size
                .array();

        RandomAccessFile accessWave = null;
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // Subchunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } catch (IOException ex) {
            // Rethrow but we still close accessWave in our finally
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }
    /**
     * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
     * Two size fields are left empty/null since we do not yet know the final stream size
     *
     * @param out         The stream to write the header to
     * @param channelMask An AudioFormat.CHANNEL_* mask
     * @param sampleRate  The sample rate in hertz
     * @param encoding    An AudioFormat.ENCODING_PCM_* value
     * @throws IOException
     */
    private void writeWavHeader(RandomAccessFile out, int channelMask, int sampleRate, int encoding)
            throws IOException {
        short channels;
        switch (channelMask) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable channel mask");
        }

        short bitDepth;
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bitDepth = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitDepth = 16;
                break;
            case AudioFormat.ENCODING_PCM_FLOAT:
                bitDepth = 32;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable encoding");
        }

        writeWavHeader(out, channels, sampleRate, bitDepth);
    }

    /**
     * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
     * Two size fields are left empty/null since we do not yet know the final stream size
     *
     * @param out        The stream to write the header to
     * @param channels   The number of channels
     * @param sampleRate The sample rate in hertz
     * @param bitDepth   The bit depth
     * @throws IOException
     */
    private void writeWavHeader(RandomAccessFile out, short channels, int sampleRate, short bitDepth)
            throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
        byte[] littleBytes = ByteBuffer.allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
        });
    }
}
