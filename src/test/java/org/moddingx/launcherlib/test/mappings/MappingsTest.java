package org.moddingx.launcherlib.test.mappings;

import net.neoforged.srgutils.IMappingFile;
import org.junit.jupiter.api.Test;
import org.moddingx.launcherlib.mappings.MappingHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MappingsTest {
    
    @Test
    public void testMappings() throws Throwable {
        IMappingFile mappingsA = IMappingFile.load(Objects.requireNonNull(MappingsTest.class.getResourceAsStream("a.tsrg")));
        IMappingFile mappingsB = IMappingFile.load(Objects.requireNonNull(MappingsTest.class.getResourceAsStream("b.tsrg")));
        IMappingFile mappingsAnm = IMappingFile.load(Objects.requireNonNull(MappingsTest.class.getResourceAsStream("a_no_meta.tsrg")));
        IMappingFile mappingsAnp = IMappingFile.load(Objects.requireNonNull(MappingsTest.class.getResourceAsStream("a_no_param.tsrg")));
        IMappingFile mappingsMerged = IMappingFile.load(Objects.requireNonNull(MappingsTest.class.getResourceAsStream("merged.tsrg")));
        
        assertMappingEqual(mappingsAnm, MappingHelper.removeMeta(mappingsA), "removeMeta does not work");
        assertMappingEqual(mappingsAnp, MappingHelper.removeParameters(mappingsA), "removeParameters does not work");
        assertMappingEqual(mappingsMerged, MappingHelper.merge(mappingsB, mappingsA), "merge does not work");
    }
    
    private static void assertMappingEqual(IMappingFile expected, IMappingFile actual, String message) throws IOException {
        // IMappingFile has no equals method, compare TSRG2 output (it is sorted, so should be the same for equal mappings)
        Path tempA = Files.createTempFile(null, ".tsrg");
        Path tempB = Files.createTempFile(null, ".tsrg");
        try {
            expected.write(tempA, IMappingFile.Format.TSRG2, false);
            actual.write(tempB, IMappingFile.Format.TSRG2, false);
            assertEquals(Files.readString(tempA), Files.readString(tempB), message);
        } finally {
            Files.deleteIfExists(tempA);
            Files.deleteIfExists(tempB);
        }
    }
}
