package org.moddingx.launcherlib.util;

import java.io.IOException;
import java.util.function.Function;

/**
 * A {@link Function} implementation that can throw {@link IOException}.
 */
@FunctionalInterface
public interface IoFunction<T, R> {

    R apply(T t) throws IOException;
}
