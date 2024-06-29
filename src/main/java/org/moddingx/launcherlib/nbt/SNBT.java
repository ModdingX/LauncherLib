package org.moddingx.launcherlib.nbt;

import org.moddingx.launcherlib.nbt.tag.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class to read and write <a href="https://minecraft.fandom.com/wiki/NBT_format#SNBT_format">SNBT</a>.
 */
public class SNBT {

    /**
     * Parses the given SNBT string.
     */
    public static Tag read(String string) throws InvalidNbtException {
        try (StringReader sr = new StringReader(string)) {
            return read(sr);
        } catch (InvalidNbtException e) {
            throw e;
        } catch (IOException e) {
            throw new IllegalStateException("IOException on string read.", e);
        }
    }
    
    /**
     * Reads SNBT from the given {@link Reader}. This will {@link Reader#close() close} the reader.
     */
    public static Tag read(Reader in) throws IOException {
        try (in; TagReader input = new TagReader(in)) {
            input.skipSpace();
            // Some SNBT files include the explicit empty tag part from binary NBT which we ignore here.
            if (input.next(2, false).equals("\"\"")) {
                input.push();
                if (!input.next(3, true).equals("\"\":")) {
                    input.push();
                }
            } else {
                input.push();
            }
            Tag tag = readTag(input);
            input.skipSpace();
            input.expectEOF();
            return tag;
        }
    }
    
    /**
     * Converts the given {@link Tag} to a string.
     */
    public static String write(Tag tag) {
        try {
            StringWriter sw = new StringWriter();
            writeTag(tag, sw, false, "");
            sw.close();
            return sw.toString();
        } catch (IOException e) {
            throw new IllegalStateException("IOException on string write.", e);
        }
    }
    
    /**
     * Writes SNBT to the given {@link Writer}. This will {@link Writer#close() close} the writer.
     */
    public static void write(Tag tag, Writer out) throws IOException {
        write(tag, out, false);
    }
    
    /**
     * Writes SNBT to the given {@link Writer}. This will {@link Writer#close() close} the writer.
     * 
     * @param pretty Whether the SNBT should be pretty-printed.
     */
    public static void write(Tag tag, Writer out, boolean pretty) throws IOException {
        try (out; BufferedWriter theWriter = new BufferedWriter(out)) {
            writeTag(tag, theWriter, pretty, "");
        }
    }
    
    private static Tag readTag(TagReader in) throws IOException {
        in.skipSpace();
        Tag result = switch (in.next()) {
            case '[' -> readListLike(in);
            case '{' -> readCompound(in);
            default -> {
                in.push();
                yield readAtom(in, false);
            }
        };
        in.skipSpace();
        return result;
    }

    private static Tag readAtom(TagReader in, boolean forceString) throws IOException {
        in.skipSpace();
        return switch (in.next()) {
            case '"' -> new StringTag(in.readUntil('"', true));
            case '\'' -> new StringTag(in.readUntil('\'', true));
            default -> {
                in.push();
                String atom = in.readUnquoted();
                if (forceString) yield new StringTag(atom);
                if ("false".equals(atom)) yield NumberTag.createByte(0);
                if ("true".equals(atom)) yield NumberTag.createByte(1);
                if (atom.endsWith("b") || atom.endsWith("B")) {
                    try {
                        yield NumberTag.createByte(Byte.parseByte(atom.substring(0, atom.length() - 1)));
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                if (atom.endsWith("s") || atom.endsWith("S")) {
                    try {
                        yield NumberTag.createShort(Short.parseShort(atom.substring(0, atom.length() - 1)));
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                if (atom.endsWith("l") || atom.endsWith("L")) {
                    try {
                        yield NumberTag.createLong(Long.parseLong(atom.substring(0, atom.length() - 1)));
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                if (atom.endsWith("f") || atom.endsWith("F")) {
                    try {
                        yield NumberTag.createFloat(Float.parseFloat(atom.substring(0, atom.length() - 1)));
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                if (atom.endsWith("d") || atom.endsWith("D")) {
                    try {
                        yield NumberTag.createDouble(Double.parseDouble(atom.substring(0, atom.length() - 1)));
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                try {
                    yield NumberTag.createInt(Integer.parseInt(atom));
                } catch (NumberFormatException e) {
                    //
                }
                try {
                    yield NumberTag.createDouble(Double.parseDouble(atom));
                } catch (NumberFormatException e) {
                    //
                }
                yield new StringTag(atom);
            }
        };
    }

    private static Tag readListLike(TagReader in) throws IOException {
        in.skipSpace();
        return switch (in.next(2, true)) {
            case "b;", "B;" -> new ByteArrayTag(readArray(in, "Byte", NumberTag::asByte));
            case "i;", "I;" -> new IntArrayTag(readArray(in, "Int", NumberTag::asInt));
            case "l;", "L;" -> new LongArrayTag(readArray(in, "Long", NumberTag::asLong));
            default -> {
                in.push();
                yield new ListTag(readList(in));
            }
        };
    }

    private static <T> List<T> readArray(TagReader in, String content, Function<NumberTag, T> extract) throws IOException {
        in.skipSpace();
        List<Tag> tags = readList(in);
        List<T> list = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof NumberTag n) list.add(extract.apply(n));
            else throw new InvalidNbtException(content + " array can't contain tags of type " + t.type());
        }
        return list;
    }

    private static List<Tag> readList(TagReader in) throws IOException {
        List<Tag> tags = new ArrayList<>();
        while (true) {
            in.skipSpace();
            if (in.next() == ']') {
                in.skipSpace();
                return tags;
            } else {
                in.push();
                tags.add(readTag(in));
                in.skipSpace();
                in.expectEither(',', ']');
            }
        }
    }

    private static Tag readCompound(TagReader in) throws IOException {
        Map<String, Tag> tags = new HashMap<>();
        while (true) {
            in.skipSpace();
            if (in.next() == '}') {
                in.skipSpace();
                return new CompoundTag(tags);
            } else {
                in.push();
                Tag keyTag = readAtom(in, true);
                if (!(keyTag instanceof StringTag)) throw new InvalidNbtException("Only strings can be keys in compound tags, got " + keyTag.type());
                String key = ((StringTag) keyTag).value();
                in.skipSpace();
                in.expect(':');
                in.skipSpace();
                Tag value = readTag(in);
                tags.put(key, value);
                in.skipSpace();
                in.expectEither(',', '}');
            }
        }
    }

    private static void writeTag(Tag tag, Writer out, boolean pretty, String indent) throws IOException {
        switch (tag.type()) {
            case BYTE -> {
                out.write(Byte.toString(((NumberTag) tag).asByte()));
                out.write("b");
            }
            case SHORT -> {
                out.write(Short.toString(((NumberTag) tag).asShort()));
                out.write("s");
            }
            case INT -> out.write(Integer.toString(((NumberTag) tag).asInt()));
            case LONG -> {
                out.write(Long.toString(((NumberTag) tag).asLong()));
                out.write("L");
            }
            case FLOAT -> {
                out.write(Float.toString(((NumberTag) tag).asFloat()));
                out.write("F");
            }
            case DOUBLE -> {
                out.write(Double.toString(((NumberTag) tag).asDouble()));
                out.write("D");
            }
            case STRING -> {
                out.write("\"");
                out.write(((StringTag) tag).value().replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'"));
                out.write("\"");
            }
            case BYTE_ARRAY -> {
                out.write(pretty ? "[B;\n" : "[B;");
                boolean first = true;
                for (byte b : (ByteArrayTag) tag) {
                    if (!first) out.write(pretty ? ",\n" : ",");
                    first = false;
                    if (pretty) out.write(indent + "  ");
                    out.write(Byte.toString(b));
                    out.write("b");
                }
                out.write(pretty ? "\n" + indent + "]" : "]");
            }
            case INT_ARRAY -> {
                out.write(pretty ? "[I;\n" : "[I;");
                boolean first = true;
                for (int i : (IntArrayTag) tag) {
                    if (!first) out.write(pretty ? ",\n" : ",");
                    first = false;
                    if (pretty) out.write(indent + "  ");
                    out.write(Integer.toString(i));
                }
                out.write(pretty ? "\n" + indent + "]" : "]");
            }
            case LONG_ARRAY -> {
                out.write(pretty ? "[L;\n" : "[L;");
                boolean first = true;
                for (long l : (LongArrayTag) tag) {
                    if (!first) out.write(pretty ? ",\n" : ",");
                    first = false;
                    if (pretty) out.write(indent + "  ");
                    out.write(Long.toString(l));
                    out.write("L");
                }
                out.write(pretty ? "\n" + indent + "]" : "]");
            }
            case LIST -> {
                out.write(pretty ? "[\n" : "[");
                boolean first = true;
                for (Tag t : (ListTag) tag) {
                    if (!first) out.write(pretty ? ",\n" : ",");
                    first = false;
                    if (pretty) out.write(indent + "  ");
                    writeTag(t, out, pretty, indent + "  ");
                }
                out.write(pretty ? "\n" + indent + "]" : "]");
            }
            case COMPOUND -> {
                out.write(pretty ? "{\n" : "{");
                boolean first = true;
                Iterable<Map.Entry<String, Tag>> itr = pretty ? ((CompoundTag) tag).stream().sorted(Map.Entry.comparingByKey()).toList() : (CompoundTag) tag;
                for (Map.Entry<String, Tag> entry : itr) {
                    if (!first) out.write(pretty ? ",\n" : ",");
                    first = false;
                    if (pretty) out.write(indent + "  ");
                    out.write("\"");
                    out.write(entry.getKey().replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'"));
                    out.write(pretty ? "\": " : "\":");
                    writeTag(entry.getValue(), out, pretty, indent + "  ");
                }
                out.write(pretty ? "\n" + indent + "}" : "}");
            }
        }
    }
    
    private static class TagReader implements Closeable {
        
        private final PushbackReader in;
        private String last;

        private TagReader(Reader in) {
            this.in = new PushbackReader(in, 8);
        }
        
        public void skipSpace() throws IOException {
            while (true) {
                int next = this.in.read();
                if (next < 0) return;
                if (!Character.isWhitespace((char) next) && !Character.isSpaceChar((char) next)) {
                    this.in.unread((char) next);
                    return;
                }
            }
        }
        
        public char next() throws IOException {
            int r = this.in.read();
            if (r < 0) throw new EOFException();
            this.last = Character.toString((char) r);
            return (char) r;
        }
        
        public String next(int len, boolean ignoreSpaces) throws IOException {
            StringBuilder res = new StringBuilder();
            StringBuilder lst = new StringBuilder();
            int cl = 0;
            while (cl < len) {
                int r = this.in.read();
                if (r < 0) break;
                lst.append((char) r);
                if (!ignoreSpaces || (!Character.isWhitespace((char) r) && !Character.isSpaceChar((char) r))) {
                    res.append((char) r);
                    cl += 1;
                }
            }
            this.last = lst.toString();
            return res.toString();
        }
        
        public void push() throws IOException {
            this.in.unread(this.last.toCharArray());
        }
        
        public String readUntil(char delim, boolean unescape) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean esc = false;
            while (true) {
                int next = this.in.read();
                if (next < 0) throw new EOFException();
                if (unescape && esc) {
                    sb.append((char) next);
                    esc = false;
                } else {
                    if (delim == (char) next) return sb.toString();
                    else if (unescape && '\\' == (char) next) esc = true;
                    else sb.append((char) next);
                }
            }
        }

        public String readUnquoted() throws IOException {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int next = this.in.read();
                if (next < 0) return sb.toString();
                char chr = (char) next;
                if ((chr >= 'A' && chr <= 'Z') || (chr >= 'a' && chr <= 'z') || (chr >= '0' && chr <= '9') || chr == '_' || chr == '-' || chr == '.' || chr == '+') {
                    sb.append(chr);
                } else {
                    this.in.unread(chr);
                    return sb.toString();
                }
            }
        }
        
        public void expect(char chr) throws IOException {
            int next = this.in.read();
            if (next < 0) throw new EOFException();
            if (chr != (char) next) throw new InvalidNbtException("Expected '" + chr + "', got '" + ((char) next) + "'");
        }
        
        public void expectEither(char skip, char noSkip) throws IOException {
            int next = this.in.read();
            if (next < 0) throw new EOFException();
            if (skip == (char) next) return;
            if (noSkip == (char) next) {
                this.in.unread((char) next);
                return;
            }
            throw new InvalidNbtException("Expected '" + skip + "' or '" + noSkip + "', got '" + ((char) next) + "'");
        }
        
        public void expectEOF() throws IOException {
            int next = this.in.read();
            if (next >= 0) throw new InvalidNbtException("Expected <EOF>, got '" + ((char) next) + "'");
        }

        @Override
        public void close() throws IOException {
            this.in.close();
        }
    }
}
