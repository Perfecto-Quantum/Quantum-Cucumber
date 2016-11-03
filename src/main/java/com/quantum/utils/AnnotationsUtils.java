package com.quantum.utils;

import cucumber.api.CucumberOptions;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnnotationsUtils {

    private static final Constructor<?> AnnotationInvocationHandler_constructor;
    private static final Constructor<?> AnnotationData_constructor;
    private static final Method Class_annotationData;
    private static final Field Class_classRedefinedCount;
    private static final Field AnnotationData_annotations;
    private static final Field AnnotationData_declaredAnotations;
    private static final Method Atomic_casAnnotationData;
    private static final Class<?> Atomic_class;
	private static final String DEFAULT_TAGS = null;
	public static final String DEFAULT_GLUE_PACKAGE = "com.quantum.steps";
	public static final String DEFAULT_FEATURES_FOLDER = "src/test/java/com/perfectomobile/quantum/features";
	public static final String DEFAULT_PLUGINS = "cucumber.runtime.formatter.CucumberPrettyFormatter";
    public static final String DEFAULT_DRYRUN = "false";
	private static final String TAGS_OPTION = "tags";
	private static final String GLUE_OPTION = "glue";
	private static final String FEATURES_OPTION = "features";
	private static final String SNIPPETS_OPTION = "snippets";
	private static final String NAME_OPTION = "name";
	private static final String FORMAT_OPTION = "format";
	private static final String MONOCHROM_OPTION = "monochrome";
	private static final String STRICT_OPTION = "strict";
	private static final String PLUGIN_OPTION = "plugin";
	private static final String DRY_RUN_OPTION = "dryRun";
    private static final String JUNIT_OPTION = "junit";
    private static final String HOOKS_PACKAGE = "com.quantum.hooks";
	
    static{
        // static initialization of necessary reflection Objects
        try {
            Class<?> AnnotationInvocationHandler_class = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            AnnotationInvocationHandler_constructor = AnnotationInvocationHandler_class.getDeclaredConstructor(new Class[]{Class.class, Map.class});
            AnnotationInvocationHandler_constructor.setAccessible(true);

            Atomic_class = Class.forName("java.lang.Class$Atomic");
            Class<?> AnnotationData_class = Class.forName("java.lang.Class$AnnotationData");

            AnnotationData_constructor = AnnotationData_class.getDeclaredConstructor(new Class[]{Map.class, Map.class, int.class});
            AnnotationData_constructor.setAccessible(true);
            Class_annotationData = Class.class.getDeclaredMethod("annotationData");
            Class_annotationData.setAccessible(true);

            Class_classRedefinedCount= Class.class.getDeclaredField("classRedefinedCount");
            Class_classRedefinedCount.setAccessible(true);

            AnnotationData_annotations = AnnotationData_class.getDeclaredField("annotations");
            AnnotationData_annotations.setAccessible(true);
            AnnotationData_declaredAnotations = AnnotationData_class.getDeclaredField("declaredAnnotations");
            AnnotationData_declaredAnotations.setAccessible(true);

            Atomic_casAnnotationData = Atomic_class.getDeclaredMethod("casAnnotationData", Class.class, AnnotationData_class, AnnotationData_class);
            Atomic_casAnnotationData.setAccessible(true);

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T extends Annotation> void putAnnotation(Class<?> c, Class<T> annotationClass, Map<String, Object> valuesMap){
        putAnnotation(c, annotationClass, annotationForMap(annotationClass, valuesMap));
    }

    public static <T extends Annotation> void putAnnotation(Class<?> c, Class<T> annotationClass, T annotation){
        try {
            while (true) { // retry loop
                int classRedefinedCount = Class_classRedefinedCount.getInt(c);
                Object /*AnnotationData*/ annotationData = Class_annotationData.invoke(c);
                // null or stale annotationData -> optimistically create new instance
                Object newAnnotationData = createAnnotationData(c, annotationData, annotationClass, annotation, classRedefinedCount);
                // try to install it
                if ((boolean) Atomic_casAnnotationData.invoke(Atomic_class, c, annotationData, newAnnotationData)) {
                    // successfully installed new AnnotationData
                    break;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> Object /*AnnotationData*/ createAnnotationData(Class<?> c, Object /*AnnotationData*/ annotationData, Class<T> annotationClass, T annotation, int classRedefinedCount) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) AnnotationData_annotations.get(annotationData);
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations= (Map<Class<? extends Annotation>, Annotation>) AnnotationData_declaredAnotations.get(annotationData);

        Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
        newDeclaredAnnotations.put(annotationClass, annotation);
        Map<Class<? extends Annotation>, Annotation> newAnnotations ;
        if (declaredAnnotations == annotations) {
            newAnnotations = newDeclaredAnnotations;
        } else{
            newAnnotations = new LinkedHashMap<>(annotations);
            newAnnotations.put(annotationClass, annotation);
        }
        return AnnotationData_constructor.newInstance(newAnnotations, newDeclaredAnnotations, classRedefinedCount);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T annotationForMap(final Class<T> annotationClass, final Map<String, Object> valuesMap){
        return (T)AccessController.doPrivileged(new PrivilegedAction<Annotation>(){
            public Annotation run(){
                InvocationHandler handler;
                try {
                    handler = (InvocationHandler) AnnotationInvocationHandler_constructor.newInstance(annotationClass,new HashMap<>(valuesMap));
                    return (Annotation)Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, handler);

                } catch (Exception e) {
                   e.printStackTrace();
                }
				return null;
            }
        });
    }
    
    @SuppressWarnings("deprecation")
	public static void setClassCucumberOptions(Class<?> targetClass, String tagsParam, String gluePackageParam, String featuresFolderParam, String pluginParam, boolean dryRun){
		CucumberOptions options = getDefaultOptions();
        Map<String, Object> valuesMap = new HashMap<>();

        String tags = StringUtils.isEmpty(tagsParam)? DEFAULT_TAGS : tagsParam;
        String glue = (StringUtils.isEmpty(gluePackageParam)? DEFAULT_GLUE_PACKAGE  : gluePackageParam) + "," + HOOKS_PACKAGE;
        String features = StringUtils.isEmpty(featuresFolderParam)? DEFAULT_FEATURES_FOLDER : featuresFolderParam;
        String plugin = StringUtils.isEmpty(pluginParam)? DEFAULT_PLUGINS : pluginParam;

        if(StringUtils.isEmpty(tagsParam)) {
            valuesMap.put(TAGS_OPTION, new String[]{});
        } else {
            valuesMap.put(TAGS_OPTION, new String[] {tags});
        }

        valuesMap.put(DRY_RUN_OPTION, dryRun);
        valuesMap.put(FEATURES_OPTION, features.split(","));
        valuesMap.put(GLUE_OPTION, glue.split(","));
        valuesMap.put(SNIPPETS_OPTION, options.snippets());
        valuesMap.put(NAME_OPTION,options.name());
        valuesMap.put(FORMAT_OPTION,options.format());
        valuesMap.put(MONOCHROM_OPTION,options.monochrome());
        valuesMap.put(STRICT_OPTION,options.strict());
        valuesMap.put(PLUGIN_OPTION, plugin.split(","));
        valuesMap.put(JUNIT_OPTION, options.junit());

        AnnotationsUtils.putAnnotation(targetClass, CucumberOptions.class, valuesMap);
        options = targetClass.getAnnotation(CucumberOptions.class);
        System.out.println("SET Cucumber Options: " + options.toString() );
    }

	private static CucumberOptions getDefaultOptions() {
		return DefaultAnnotations.class.getAnnotation(CucumberOptions.class);
	}
	
	@CucumberOptions()
	private class DefaultAnnotations {
		
	}
}