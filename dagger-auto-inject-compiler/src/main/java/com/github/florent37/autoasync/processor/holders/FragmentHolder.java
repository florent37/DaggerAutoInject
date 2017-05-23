package com.github.florent37.autoasync.processor.holders;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;

public class FragmentHolder {
    public Element element;
    public ClassName classNameComplete;
    public String className;

    public FragmentHolder(Element element, ClassName classNameComplete, String className) {
        this.element = element;
        this.classNameComplete = classNameComplete;
        this.className = className;
    }
    
}
