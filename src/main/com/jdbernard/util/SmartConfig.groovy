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

        try { file.withInputStream { is -> this.@props.load(is) } }
        catch (FileNotFoundException fnfe) {}
        catch (Exception e) { log.warn("Cannot open config file.", e) }
    }

    public SmartConfig(String filename) { this(new File(filename)) }

    public save() { 
        try {file.withOutputStream { os -> this.@props.store(os, "") } }
        catch (Exception e) { log.warn("Cannot save config file.", e) }
    }

    def getProperty(String name) { getProperty(name, "") }

    def getProperty(String name, Object defVal) {

        if (log.isTraceEnabled()) log.trace("Looking up $name")

        def val = props.getProperty(name)

        if (val == null) {
            if (log.isTraceEnabled())
                log.trace("Doesn't exists, setting with given default")

            val = defVal
            props.setProperty(name, defVal.toString())

        } else {

            // boolean
            if (name ==~ /.*\?/) {
                if (log.isTraceEnabled()) log.trace("Interpreting as a boolean")

                if (val.toLowerCase() =~ /(false|no|off|f|n)/)
                    val = false
                else val = (Boolean) val
            }
        }

        return val.asType(defVal.class)
    }

    void setProperty(String name, def value) {
        props."$name" = value.toString()
        save()
    }
}
