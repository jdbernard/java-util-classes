package com.jdbernard.util

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

public class JarUtils {

    public static void extract(File f, File outputDir) {
        extract(new JarFile(f), outputDir) }

    public static void extract(JarFile jarFile, File outputDir) {
        jarFile.entries().each { jarEntry ->
            def outputFile = new File(outputDir, jarEntry.name)
            if (jarEntry.isDirectory()) { outputFile.mkdirs() }
            else {
                jarFile.getInputStream(jarEntry).withStream { is ->
                    outputFile.withOutputStream { os ->
                        while (is.available() > 0) { os.write(is.read()) }}}}}}

    public static void extract(JarInputStream jarIS, File outputDir) {
        JarEntry curEntry = jarIS.nextJarEntry
        while(curEntry != null) {
            File outFile = new File(outputDir, curEntry.name)
            if (curEntry.isDirectory()) { outFile.mkdirs() }
            else {
                outFile.withOutputStream { os ->
                    byte[] buffer = new byte[curEntry.size]
                    int bytesRead = jarIS.read(buffer, 0, curEntry.size as
                    Integer)
                    os.write(buffer, 0, bytesRead) }}
            
            curEntry = jarIS.nextJarEntry }
        jarIS.close() }
}
