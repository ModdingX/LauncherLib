package org.moddingx.launcherlib.test.nbt;

import org.junit.jupiter.api.Test;
import org.moddingx.launcherlib.nbt.TagType;
import org.moddingx.launcherlib.nbt.tag.ListTag;
import org.moddingx.launcherlib.nbt.tag.StringTag;

import static org.junit.jupiter.api.Assertions.*;

public class TagTest {
    
    @Test
    public void testElementType() {
        ListTag tag = new ListTag();
        assertEquals(TagType.END, tag.elementType(), "Empty list should have END element type");
        
        tag.addInt(12);
        assertEquals(TagType.INT, tag.elementType(), "Wrong element type");
        
        tag.setString(0, "Hello, world!");
        assertEquals(TagType.STRING, tag.elementType(), "Wrong element type");
        
        tag.remove(0);
        assertEquals(TagType.END, tag.elementType(), "Empty list should have END element type");
        
        tag.addInt(12);
        tag.addInt(42);
        assertThrows(IllegalArgumentException.class, () -> tag.set(1, new StringTag("Hello, world!")));
        assertThrows(IllegalArgumentException.class, () -> tag.addDouble(2));
    }
}
