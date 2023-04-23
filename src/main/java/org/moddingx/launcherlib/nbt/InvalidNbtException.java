package org.moddingx.launcherlib.nbt;

import java.io.IOException;

/**
 * Thrown when attempting to parse invalid {@link NBT NBT}.
 */
public class InvalidNbtException extends IOException {

    public InvalidNbtException(String message) {
        super(message);
    }

    public InvalidNbtException(String message, Throwable cause) {
        super(message, cause);
    }
}
