package org.groocss

import org.codehaus.groovy.control.CompilerConfiguration

import groovy.transform.*

@CompileStatic
class GrooCSS extends Script {
    
    static class Wrapper {
        String name
        List<StyleGroup> groups = []
        List<KeyFrames> kfs = []
        void leftShift(StyleGroup sg) { groups << sg }
        void leftShift(KeyFrames kf) { kfs << kf }
        String toString() {
            String str = ''
            if (name) str += "$name {\n${groups.join('\n')}\n}"
            else str += groups.join('\n')
            if (kfs) str += kfs.join('\n')
            str
        }
    }

    static class KeyFrames {
        String name
        List<StyleGroup> groups = []
        void leftShift(StyleGroup sg) { groups << sg }
        String toString() {
            if (name) "$name {\n${groups.join('\n')}\n}"
            else groups.join('\n')
        }

        KeyFrames frame(int percent, @DelegatesTo(StyleGroup) Closure clos) {
            frame([percent], clos)
        }

        KeyFrames frame(List<Integer> percents, @DelegatesTo(StyleGroup) Closure clos) {
            StyleGroup sg = new StyleGroup(selector: percents.collect{"${it}%"}.join(", "))
            clos.delegate = sg
            clos()
            this << sg
            this
        }
    }

    static void convert(String inFilename, String outFilename) {
        convert(new File(inFilename), new File(outFilename))
    }
    
    static void convert(File inf, File out) {
        def binding = new Binding()
        def config = new CompilerConfiguration()
        config.scriptBaseClass = 'org.groocss.GrooCSS'
        def shell = new GroovyShell(this.class.classLoader, binding, config)
        
        Wrapper css = (Wrapper) shell.evaluate(inf)
        
        out.withPrintWriter { pw ->
            css.groups.each { pw.println it }
        }
    }
    
    static void main(String ... args) {
        if (args.length == 1)
            convert(args[0], args[0].replace('.groocss', '.css'))
    }
    
    Wrapper css = new Wrapper()

    public String toString() { css.toString() }

    Wrapper keyframes(String name, @DelegatesTo(KeyFrames) Closure clos) {
        kf(name, clos)
    }

    Wrapper kf(String name, @DelegatesTo(KeyFrames) Closure clos) {
        KeyFrames frames = new KeyFrames(name: "@keyframes $name")
        clos.delegate = frames
        clos()
        css << frames
        css
    }

    Wrapper sel(String selector, @DelegatesTo(StyleGroup) Closure clos) {
        StyleGroup sg = new StyleGroup(selector: selector)
        clos.delegate = sg
        clos()
        css << sg
        css
    }

    Wrapper sg(String selector, @DelegatesTo(StyleGroup) Closure clos) {
        sel(selector, clos)
    }
    
    Style style(@DelegatesTo(Style) Closure clos) {
        Style s = new Style()
        clos.delegate = s
        clos()
        s
    }
    
    def run() {}

    static GrooCSS process(@DelegatesTo(GrooCSS) Closure clos) {
        runBlock(clos)
    }

    static GrooCSS runBlock(@DelegatesTo(GrooCSS) Closure clos) {
        GrooCSS gcss = new GrooCSS()
        clos.delegate = gcss
        clos()
        gcss
    }
   
}
