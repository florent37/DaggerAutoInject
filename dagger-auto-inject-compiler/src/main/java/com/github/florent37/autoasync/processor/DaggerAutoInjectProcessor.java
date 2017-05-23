package com.github.florent37.autoasync.processor;

import com.github.florent37.autoasync.processor.holders.ActivityHolder;
import com.github.florent37.autoasync.processor.holders.ApplicationHolder;
import com.github.florent37.autoasync.processor.holders.FragmentHolder;
import com.github.florent37.daggerautoinject.InjectActivity;
import com.github.florent37.daggerautoinject.InjectApplication;
import com.github.florent37.daggerautoinject.InjectFragment;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes({
        "com.github.florent37.daggerautoinject.InjectActivity",
        "com.github.florent37.daggerautoinject.InjectFragment",
        "com.github.florent37.daggerautoinject.InjectApplication"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(javax.annotation.processing.Processor.class)
public class DaggerAutoInjectProcessor extends AbstractProcessor {

    private Map<ClassName, ActivityHolder> activityHolders = new HashMap<>();
    private Map<ClassName, FragmentHolder> fragmentHolders = new HashMap<>();
    private ApplicationHolder applicationHolder;
    private Filer filer;

    private static TypeMirror getComponent(InjectApplication annotation) {
        try {
            annotation.component(); // this should throw
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null; // can this ever happen ??
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        processAnnotations(env);

        if(applicationHolder != null) {
            writeHoldersOnJavaFile();
        }
        return true;
    }

    protected void processAnnotations(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(InjectActivity.class)) {
            final ClassName classFullName = ClassName.get((TypeElement) element); //com.github.florent37.sample.TutoAndroidFrance
            final String className = element.getSimpleName().toString(); //TutoAndroidFrance
            activityHolders.put(classFullName, new ActivityHolder(element, classFullName, className));
        }

        for (Element element : env.getElementsAnnotatedWith(InjectFragment.class)) {
            final ClassName classFullName = ClassName.get((TypeElement) element); //com.github.florent37.sample.TutoAndroidFrance
            final String className = element.getSimpleName().toString(); //TutoAndroidFrance
            fragmentHolders.put(classFullName, new FragmentHolder(element, classFullName, className));
        }

        for (Element element : env.getElementsAnnotatedWith(InjectApplication.class)) {
            final ClassName classFullName = ClassName.get((TypeElement) element); //com.github.florent37.sample.TutoAndroidFrance
            final String className = element.getSimpleName().toString(); //TutoAndroidFrance

            final TypeMirror componentClass = getComponent(element.getAnnotation(InjectApplication.class));

            applicationHolder = new ApplicationHolder(element, classFullName, className);
            applicationHolder.setComponentClass(componentClass);
        }
    }

    protected void writeHoldersOnJavaFile() {
        constructActivityModule();
        constructFragmentModule();
        construct();

        fragmentHolders.clear();
        activityHolders.clear();
        applicationHolder = null;
    }

    private void constructActivityModule() {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(Constants.ACTIVITY_MODULE)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(Constants.DAGGER_MODULE);

        for (ActivityHolder activityHolder : activityHolders.values()) {
            builder.addMethod(MethodSpec.methodBuilder(Constants.METHOD_CONTRIBUTE + activityHolder.className)
                    .addAnnotation(Constants.DAGGER_ANDROID_ANNOTATION)
                    .addModifiers(Modifier.ABSTRACT)
                    .returns(activityHolder.classNameComplete)
                    .build()
            );
        }

        final TypeSpec newClass = builder.build();
        final JavaFile javaFile = JavaFile.builder(Constants.PACKAGE_NAME, newClass).build();

        try {
            javaFile.writeTo(System.out);
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void constructFragmentModule() {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(Constants.FRAGMENT_MODULE)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(Constants.DAGGER_MODULE);

        for (FragmentHolder fragmentHolder : fragmentHolders.values()) {
            builder.addMethod(MethodSpec.methodBuilder(Constants.METHOD_CONTRIBUTE + fragmentHolder.className)
                    .addAnnotation(Constants.DAGGER_ANDROID_ANNOTATION)
                    .addModifiers(Modifier.ABSTRACT)
                    .returns(fragmentHolder.classNameComplete)
                    .build()
            );
        }

        final TypeSpec newClass = builder.build();
        final JavaFile javaFile = JavaFile.builder(Constants.PACKAGE_NAME, newClass).build();

        try {
            javaFile.writeTo(System.out);
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void construct() {

        final TypeSpec.Builder builder = TypeSpec.classBuilder(Constants.MAIN_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC);

        builder.addField(FieldSpec.builder(ClassName.get(String.class), "TAG", Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC)
                .initializer("\"" + Constants.MAIN_CLASS_NAME + "\"").build());

        builder.addMethod(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build()
        );

        //final ClassName daggerComponent = findDaggerComponent(applicationHolder.componentClass);
        final ClassName component = findComponent(applicationHolder.componentClass);

        {
            final MethodSpec.Builder methodInit = MethodSpec.methodBuilder(Constants.METHOD_INIT)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            if (applicationHolder != null) {

                methodInit
                        .addParameter(applicationHolder.classNameComplete, Constants.PARAM_APPLICATION)
                        .addParameter(component, Constants.PARAM_COMPONENT);

                    methodInit.addStatement("$L.inject($L)", Constants.PARAM_COMPONENT, Constants.PARAM_APPLICATION);
            }


            methodInit.addStatement("application.registerActivityLifecycleCallbacks(new $T.ActivityLifecycleCallbacks() {\n" +
                            "            @Override\n" +
                            "            public void onActivityCreated($T activity, $T savedInstanceState) {\n" +
                            "                " + Constants.METHOD_HANDLE_ACTIVITY + "(activity);\n" +
                            "            }\n" +
                            "\n" +
                            "            @Override\n" +
                            "            public void onActivityStarted(Activity activity) {\n" +
                            "\n" +
                            "            }\n" +
                            "\n" +
                            "            @Override\n" +
                            "            public void onActivityResumed(Activity activity) {\n" +
                            "\n" +
                            "            }\n" +
                            "\n" +
                            "            @Override\n" +
                            "            public void onActivityPaused(Activity activity) {\n" +
                            "\n" +
                            "            }\n" +
                            "\n" +
                            "            @Override\n" +
                            "            public void onActivityStopped(Activity activity) {\n" +
                            "\n" +
                            "            }\n" +
                            "\n" +
                            "            @Override\n" +
                            "            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {\n" +
                            "\n" +
                            "            }\n" +
                            "\n" +
                            "            @Override\n" +
                            "            public void onActivityDestroyed(Activity activity) {\n" +
                            "\n" +
                            "            }\n" +
                            "        });",

                    Constants.APPLICATION,
                    Constants.ACTIVITY,
                    Constants.BUNDLE
            );

            builder.addMethod(methodInit.build());
        }

        /*
        {
            final MethodSpec.Builder methodInit = MethodSpec.methodBuilder(Constants.METHOD_INIT)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            if (applicationHolder != null) {
                methodInit
                        .addParameter(applicationHolder.classNameComplete, Constants.PARAM_APPLICATION);

                if (applicationHolder.componentClass != null) {
                    methodInit.addCode("$T component = $T.builder()\n" +
                                    "                .application(" + Constants.PARAM_APPLICATION + ")\n" +
                                    "                .build();\n",
                            component, daggerComponent);
                    methodInit.addStatement("$L($L, $L)", Constants.METHOD_INIT, Constants.PARAM_APPLICATION, Constants.PARAM_COMPONENT);
                }
            }
            builder.addMethod(methodInit.build());
        }
        */

        {
            final MethodSpec.Builder methodHandleActivity = MethodSpec.methodBuilder(Constants.METHOD_HANDLE_ACTIVITY)
                    .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                    .addParameter(Constants.ACTIVITY, "activity");

            methodHandleActivity.addCode("try {\n" +
                            "            $T.inject(activity);\n" +
                            "        } catch (Exception e){\n" +
                            "            $T.d(TAG, activity.getClass().toString()+\" non injected\");\n" +
                            "        }\n" +
                            "        if (activity instanceof $T) {\n" +
                            "            final $T supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();\n" +
                            "            supportFragmentManager.registerFragmentLifecycleCallbacks(\n" +
                            "                    new FragmentManager.FragmentLifecycleCallbacks() {\n" +
                            "                        @Override\n" +
                            "                        public void onFragmentCreated(FragmentManager fm, $T f, $T savedInstanceState) {\n" +
                            "                            try {\n" +
                            "                                $T.inject(f);\n" +
                            "                            } catch (Exception e){\n" +
                            "                                Log.d(TAG, f.getClass().toString()+\" non injected\");\n" +
                            "                            }\n" +
                            "                        }\n" +
                            "                    }, true);\n" +
                            "        }",

                    Constants.ANDROID_INJECTION,
                    Constants.ANDROID_LOG,
                    Constants.FRAGMENT_ACTIVITY,
                    Constants.FRAGMENT_MANAGER,
                    Constants.FRAGMENT,
                    Constants.BUNDLE,
                    Constants.ANDROID_SUPPORT_INJECTION
            );

            builder.addMethod(methodHandleActivity.build());
        }

        final TypeSpec newClass = builder.build();

        final JavaFile javaFile = JavaFile.builder(Constants.PACKAGE_NAME, newClass).build();

        try {
            javaFile.writeTo(System.out);
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ClassName findDaggerComponent(TypeMirror typeMirror) {
        final ClassName typeName = (ClassName) TypeName.get(typeMirror);
        final String packageName = typeName.packageName();
        final String className = typeName.simpleName();
        return ClassName.bestGuess(packageName + "." + Constants.DAGGER + className);
    }

    private ClassName findComponent(TypeMirror typeMirror) {
        final ClassName typeName = (ClassName) TypeName.get(typeMirror);
        final String packageName = typeName.packageName();
        final String className = typeName.simpleName();
        return ClassName.bestGuess(packageName + "." + className);
    }

}
