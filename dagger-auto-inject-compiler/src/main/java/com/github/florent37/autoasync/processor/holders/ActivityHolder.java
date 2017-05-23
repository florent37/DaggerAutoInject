package com.github.florent37.autoasync.processor.holders;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

public class ActivityHolder {
    public Element element;
    public ClassName classNameComplete;
    public String className;

    public ActivityHolder(Element element, ClassName classNameComplete, String className) {
        this.element = element;
        this.classNameComplete = classNameComplete;
        this.className = className;
    }

}
