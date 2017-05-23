package com.github.florent37.autoasync.processor.holders;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public class ApplicationHolder {
    public Element element;
    public ClassName classNameComplete;
    public String className;

    public TypeMirror componentClass;

    public ApplicationHolder(Element element, ClassName classNameComplete, String className) {
        this.element = element;
        this.classNameComplete = classNameComplete;
        this.className = className;
    }

    public void setComponentClass(TypeMirror componentClass) {
        this.componentClass = componentClass;
    }
}
