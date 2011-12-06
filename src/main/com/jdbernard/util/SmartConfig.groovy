package com.jdbernard.util

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartConfig {

    private File file
    private Properties props
    private Logger log = LoggerFactory.getLogger(getClass())

    public SmartConfig(File file) {
        this.@file = file
        this.@props = new Properties()

        this.load()
    }

    public SmartConfig(String filename) {
        this(new File(filename))

        log.trace("Loading configuration from {}",
            new File(filename).canonicalPath)
    }

    public synchronized load() {
        log.trace("Loading configuration from {}", file.canonicalPath)

        try { this.@file.withInputStream { is -> this.@props.load(is) } }
        catch (FileNotFoundException fnfe) {}
        catch (Exception e) { log.warn("Cannot open config file.", e) }
    }

    public synchronized save() {
        log.trace("Saving changes.")
        try {file.withOutputStream { os -> this.@props.store(os, "") } }
        catch (Exception e) { log.warn("Cannot save config file.", e) }
    }

    def getProperty(String name) { getProperty(name, null) }

    def getProperty(String name, Object defVal) {

        log.trace("Looking up {}", name)

        def val = props[name]

        if (val == null && defVal != null) {
            log.trace("Doesn't exists, setting with given default")

            val = defVal
            this.setProperty(name, defVal)

        } else {

            // boolean
            if (name ==~ /.*\?/) {
                log.trace("Interpreting as a boolean")

                if (val.toLowerCase() =~ /(false|no|off|f|n)/)
                    val = false
                else val = (Boolean) val
            }

            // directory or file
            else if (name ==~ /.*([dD]irectory|[dD]ir|[fF]ile)/) {
                log.trace("Interpreting as a directory.")

                val = new File(val)
            } else {
                log.trace("No known interpretation, casting to type of " +
                    "default argument.")

                val = val.asType(defVal.class)
            }
        }

        return val
    }

    void setProperty(String name, def value) {
        log.trace("Setting property: {}", name)

        if (name ==~ /.*([dD]irectory|[dD]ir|[fF]ile)/) {
            log.trace("Interpreting as a file/directory.")
            if (!(value instanceof File)) { value = new File(value) }
            props[name] = value.canonicalPath
        } else {
            props[name] = value.toString()
        }
        save()
    }

    public Set<String> keySet() { return props.keySet() }
}
