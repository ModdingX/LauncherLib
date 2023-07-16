package org.moddingx.launcherlib.test.util;

import org.junit.jupiter.api.Test;
import org.moddingx.launcherlib.util.Artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArtifactTest {
    
    @Test
    public void testDescriptorParsing() throws Throwable {
        assertEquals(Artifact.from("org.moddingx", "LauncherLib", "1.0.0", null, null), Artifact.from("org.moddingx:LauncherLib:1.0.0"));
        assertEquals(Artifact.from("org.moddingx", "LauncherLib", "1.0.0", null, "zip"), Artifact.from("org.moddingx:LauncherLib:1.0.0@zip"));
        assertEquals(Artifact.from("org.moddingx", "LauncherLib", "1.0.0", "sources", null), Artifact.from("org.moddingx:LauncherLib:1.0.0:sources"));
        assertEquals(Artifact.from("org.moddingx", "LauncherLib", "1.0.0", "sources", "zip"), Artifact.from("org.moddingx:LauncherLib:1.0.0:sources@zip"));
        assertThrows(IllegalArgumentException.class, () -> Artifact.from("org.moddingx:LauncherLib:1.0.0:sources:fatjar"));
        assertThrows(IllegalArgumentException.class, () -> Artifact.from("org.moddingx:LauncherLib:1.0.0@zip@jar"));
    }
    
    @Test
    public void testComparison() throws Throwable {
        Artifact[] sorted = new Artifact[]{
                Artifact.from("net.minecraft", "client", "1.7.10"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.0"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.0", null, "pom"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.0", "changelog", "txt"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.0", "sources"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.0", "sources", "txt"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.0", "sources", "zip"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.1"),
                Artifact.from("org.moddingx", "LauncherLib", "1.0.15"),
                Artifact.from("org.moddingx", "LauncherLib", "1.2.3"),
                Artifact.from("org.moddingx", "LauncherLib", "1.2.3.4"),
                Artifact.from("org.moddingx", "LauncherLib", "1.2.4"),
                Artifact.from("org.moddingx", "LauncherLib", "2"),
                Artifact.from("org.moddingx", "LauncherLib", "2.2"),
                Artifact.from("org.moddingx", "LauncherLib", "2.2-1"),
                Artifact.from("org.moddingx", "LauncherLib", "2.2-2"),
                Artifact.from("org.moddingx", "LauncherLib", "2.2-2.2.3"),
                Artifact.from("org.moddingx", "LauncherLib", "2.2-2.9"),
                Artifact.from("org.moddingx", "LauncherLib", "2.2-2.12"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3-alpha-1"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3-alpha-3"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3-beta-2"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3-pre"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3-rc-1-7-3"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3-rc-1-61-3"),
                Artifact.from("org.moddingx", "LauncherLib", "2.3.0"),
                Artifact.from("org.moddingx", "LibX", "2.0.4")
        };
        for (int i = 0; i < sorted.length; i++) {
            for (int j = 0; j < sorted.length; j++) {
                assertEquals(Integer.compare(i, j), sorted[i].compareTo(sorted[j]), sorted[i] + " <=> " + sorted[j]);
                assertEquals(Integer.compare(j, i), sorted[j].compareTo(sorted[i]), sorted[j] + " <=> " + sorted[i]);
            }
        }
    }
}
