package org.moddingx.launcherlib.nbt;

import org.moddingx.launcherlib.nbt.tag.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class to read and write binary <a href="https://minecraft.fandom.com/wiki/NBT_format#Binary_format">NBT</a>.
 */
public class NBT {

    /**
     * Reads GZIP compressed NBT from the given {@link InputStream}. This will {@link InputStream#close() close} the stream.
     */
    public static Tag read(InputStream in) throws IOException {
        return read(in, Compression.GZIP);
    }
    
    /**
     * Reads NBT from the given {@link InputStream}. This will {@link InputStream#close() close} the stream.
     */
    public static Tag read(InputStream in, Compression compression) throws IOException {
        try (in; DataInputStream input = switch (compression) {
            case NONE -> in instanceof DataInputStream di ? di : (in instanceof BufferedInputStream ? new DataInputStream(in) : new DataInputStream(new BufferedInputStream(in)));
            case GZIP -> new DataInputStream(new BufferedInputStream(new GZIPInputStream(in)));
            case DEFLATE -> new DataInputStream(new BufferedInputStream(new DeflaterInputStream(in)));
        }) {
            return readCompound(input, true);
        } catch (EOFException e) {
            throw new InvalidNbtException("End of stream", e);
        }
    }
    
    /**
     * Writes GZIP compressed NBT to the given {@link OutputStream}. This will {@link OutputStream#close() close} the stream.
     */
    public static void write(Tag tag, OutputStream out) throws IOException {
        write(tag, out, Compression.GZIP);
    }
    
    /**
     * Writes NBT to the given {@link OutputStream}. This will {@link OutputStream#close() close} the stream.
     */
    public static void write(Tag tag, OutputStream out, Compression compression) throws IOException {
        try (out; DataOutputStream output = switch (compression) {
            case NONE -> out instanceof DataOutputStream dd ? dd : (out instanceof BufferedOutputStream ? new DataOutputStream(out) : new DataOutputStream(new BufferedOutputStream(out)));
            case GZIP -> new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(out)));
            case DEFLATE -> new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(out)));
        }) {
            output.writeByte(tag.type().id());
            output.writeUTF("");
            writeTag(tag, output);
        }
    }
    
    private static Tag readTag(TagType type, DataInput input) throws IOException {
        return switch (type) {
            case END -> throw new InvalidNbtException("No tag to end.");
            case BYTE -> NumberTag.createByte(input.readByte());
            case SHORT -> NumberTag.createShort(input.readShort());
            case INT -> NumberTag.createInt(input.readInt());
            case LONG -> NumberTag.createLong(input.readLong());
            case FLOAT -> NumberTag.createFloat(input.readFloat());
            case DOUBLE -> NumberTag.createDouble(input.readDouble());
            case STRING -> new StringTag(input.readUTF());
            case BYTE_ARRAY -> {
                int len = input.readInt();
                byte[] data = new byte[len];
                input.readFully(data);
                yield new ByteArrayTag(data);
            }
            case INT_ARRAY -> {
                int len = input.readInt();
                int[] data = new int[len];
                for (int i = 0; i < len ; i++) data[i] = input.readInt();
                yield new IntArrayTag(data);
            }
            case LONG_ARRAY -> {
                int len = input.readInt();
                long[] data = new long[len];
                for (int i = 0; i < len ; i++) data[i] = input.readLong();
                yield new LongArrayTag(data);
            }
            case LIST -> {
                TagType elementType = TagType.get(input.readByte());
                int len = input.readInt();
                List<Tag> elements = new ArrayList<>(len);
                for (int i = 0; i < len ; i++) elements.add(readTag(elementType, input));
                yield new ListTag(elements);
            }
            case COMPOUND -> readCompound(input, false);
        };
    }
    
    private static Tag readCompound(DataInput input, boolean single) throws IOException {
        Map<String, Tag> tags = new HashMap<>();
        while (true) {
            TagType type = TagType.get(input.readByte());
            if (type == TagType.END) return new CompoundTag(tags);
            String key = input.readUTF();
            Tag value = readTag(type, input);
            if (single) {
                if (key.isEmpty()) return value;
                tags.put(key, value);
                return new CompoundTag(tags);
            } else {
                tags.put(key, value);
            }
        }
    }
    
    private static void writeTag(Tag tag, DataOutput output) throws IOException {
        switch (tag.type()) {
            case BYTE -> output.writeByte(((NumberTag) tag).asByte());
            case SHORT -> output.writeShort(((NumberTag) tag).asShort());
            case INT -> output.writeInt(((NumberTag) tag).asInt());
            case LONG -> output.writeLong(((NumberTag) tag).asLong());
            case FLOAT -> output.writeFloat(((NumberTag) tag).asFloat());
            case DOUBLE -> output.writeDouble(((NumberTag) tag).asDouble());
            case STRING -> output.writeUTF(((StringTag) tag).value());
            case BYTE_ARRAY -> {
                ByteArrayTag bt = (ByteArrayTag) tag;
                output.writeInt(bt.size());
                for (byte b : bt) output.writeByte(b);
            }
            case INT_ARRAY -> {
                IntArrayTag it = (IntArrayTag) tag;
                output.writeInt(it.size());
                for (int i : it) output.writeInt(i);
            }
            case LONG_ARRAY -> {
                LongArrayTag lt = (LongArrayTag) tag;
                output.writeInt(lt.size());
                for (long l : lt) output.writeLong(l);
            }
            case LIST -> {
                ListTag lt = (ListTag) tag;
                TagType elementType = lt.elementType();
                output.writeByte(elementType.id());
                output.writeInt(lt.size());
                for (Tag t : lt) {
                    if (t.type() != elementType) throw new IllegalStateException("Wrong element in list, expected " + elementType + ", got " + t.type());
                    writeTag(t, output);
                }
            }
            case COMPOUND -> writeCompound((CompoundTag) tag, output);
        }
    }
    
    private static void writeCompound(CompoundTag tag, DataOutput output) throws IOException {
        for (Map.Entry<String, Tag> entry : tag) {
            output.writeByte(entry.getValue().type().id());
            output.writeUTF(entry.getKey());
            writeTag(entry.getValue(), output);
        }
        output.writeByte(TagType.END.id());
    }

    /**
     * A compression method used to read and write NBT.
     */
    public enum Compression {

        /**
         * Uncompressed data. This is for example used in the {@code servers.dat} file.
         */
        NONE,

        /**
         * GZIP compression. This is the default in most cases.
         */
        GZIP,
        
        /**
         * DEFLATE compression. Rarely used in minecraft.
         */
        DEFLATE
    }
}
