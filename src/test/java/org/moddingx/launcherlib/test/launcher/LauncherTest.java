package org.moddingx.launcherlib.test.launcher;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.moddingx.launcherlib.launcher.Launcher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LauncherTest {
    
    @Test
    @Disabled
    public void testThatAllVersionsCanBeParsed() throws Throwable {
        Launcher launcher = new Launcher();
        List<String> versions = assertDoesNotThrow(launcher::versions, "Failed to load list of versions.");
        for (String version : versions) {
            assertDoesNotThrow(() -> launcher.version(version), "Failed to load version " + version);
        }
    }
}
