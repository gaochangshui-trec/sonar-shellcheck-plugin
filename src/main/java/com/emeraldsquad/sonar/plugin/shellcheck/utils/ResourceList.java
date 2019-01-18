package com.emeraldsquad.sonar.plugin.shellcheck.utils;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * list resources available from the classpath @ *
 */
public class ResourceList {
    private static final Logger LOGGER = Loggers.get(ResourceList.class);

    private ResourceList(){}

    public static List<String> getDocList() {
        List<String> list = new ArrayList<>();
        CodeSource src = ResourceList.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                    while(true) {
                        ZipEntry e = zip.getNextEntry();
                        if (e == null)
                            break;
                        String name = e.getName();
                        if (name.startsWith("doc/SC")) {
                            list.add(name);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Error while loading bash rules definitions", e);
                }
            }

        return list;
    }
}  