package org.moddingx.launcherlib;

import net.minecraftforge.srgutils.IMappingFile;
import org.moddingx.launcherlib.launcher.Launcher;

import java.io.IOException;
import java.nio.file.Paths;

public class Test {
    
    public static void main(String[] args) throws IOException {
        IMappingFile mappings = new Launcher().version("23w16a").mergedMap();
        mappings.write(Paths.get("/tmp/merged.tsrg"), IMappingFile.Format.TSRG2, false);
    }
}
