package org.moddingx.launcherlib.test.nbt;

import org.junit.jupiter.api.Test;
import org.moddingx.launcherlib.nbt.InvalidNbtException;
import org.moddingx.launcherlib.nbt.NBT;
import org.moddingx.launcherlib.nbt.SNBT;
import org.moddingx.launcherlib.nbt.tag.*;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class IoTest {
    
    private final CompoundTag nbt;
    
    public IoTest() {
        this.nbt = new CompoundTag();
        this.nbt.put("bytes", new ByteArrayTag(new byte[]{ 1, 0, 14, 13 }));
        this.nbt.put("cmp", new CompoundTag(Map.of(
                "12", NumberTag.createInt(0),
                "a", NumberTag.createShort(1),
                "\"", new StringTag("A test string")
        )));
        this.nbt.put("'cmp'", new CompoundTag(Map.of("ints", new IntArrayTag(new int[]{ 123, 124, 7, 0 }))));
        this.nbt.put("list", new ListTag(List.of(
                new LongArrayTag(new long[]{ 12, 14, 23, -44, -17 }),
                new LongArrayTag()
        )));
        this.nbt.putDouble("dbl", 2);
        this.nbt.putFloat("f", 1);
    }
    
    @Test
    public void testRead() throws Throwable {
        Tag parsedNBT = NBT.read(Objects.requireNonNull(IoTest.class.getResourceAsStream("data_in.nbt")));
        Tag parsedSNBT = SNBT.read(new InputStreamReader(Objects.requireNonNull(IoTest.class.getResourceAsStream("data_in.snbt"))));
        Tag parsedSNBT2 = SNBT.read(new InputStreamReader(Objects.requireNonNull(IoTest.class.getResourceAsStream("data_in2.snbt"))));
        
        assertEquals(this.nbt, parsedNBT, "Binary NBT failed to parse correctly");
        assertEquals(this.nbt, parsedSNBT, "String NBT failed to parse correctly");
        assertEquals(this.nbt, parsedSNBT2, "Explicit string NBT failed to parse correctly");
        assertThrows(InvalidNbtException.class, () -> SNBT.read(new InputStreamReader(Objects.requireNonNull(IoTest.class.getResourceAsStream("data_in_invalid.snbt")))), "String with space detected as empty in SNBT.");
    }
    
    @Test
    public void testWrite() throws Throwable {
        String expectedSNBT;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Objects.requireNonNull(IoTest.class.getResourceAsStream("data_out.snbt"))))) {
            expectedSNBT = r.lines().collect(Collectors.joining("\n"));
        }

        StringWriter sw = new StringWriter();
        SNBT.write(this.nbt, sw, true);
        sw.close();
        assertEquals(expectedSNBT, sw.toString(), "String NBT failed to write correctly");
        
        // Maps have no order, so non-pretty SNBT output and binary nbt output can't be tested that way.
        // Check that we at least can read it back in and that non-pretty snbt does not contain any extra spaces.
        
        String outputSNBT = SNBT.write(this.nbt);
        long spaces = outputSNBT.chars().filter(chr -> chr == ' ').count();
        assertEquals(2, spaces, "Non-pretty SNBT has too many spaces.");
        assertFalse(outputSNBT.contains("\n"), "Non-pretty SNBT should not contain line-breaks.");
        
        assertEquals(this.nbt, SNBT.read(outputSNBT), "Non-pretty SNBT failed to re-read.");


        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        NBT.write(this.nbt, byteOut);
        byteOut.close();
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        assertEquals(this.nbt, NBT.read(byteIn), "Binary NBT failed to re-read.");
        byteIn.close();
    }
}
