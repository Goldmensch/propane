package dev.goldmensch.propane.spec.processor.javadoc;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public interface JavaDocReader {
    Comment EMPTY_COMMENT = new Comment("", List.of());

    Comment get(Element element);

    record Comment(String comment, Collection<TypeElement> importedTypes) {}

    static JavaDocReader choose(ProcessingEnvironment env) {
        if(exists("com.sun.source.util.DocTrees")) {
            return load("OpenJDKJavaDocReader", env);
        }

        env.getMessager().printWarning("Copying javadocs is only supported if using OpenJDK.");

        return _ -> EMPTY_COMMENT;
    }

    private static JavaDocReader load(String name, ProcessingEnvironment env) {
        try {
            Class<?> readerClass = Class.forName("dev.goldmensch.propane.spec.processor.javadoc." + name);
            return (JavaDocReader) readerClass.getDeclaredConstructor(ProcessingEnvironment.class).newInstance(env);

        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean exists(String klass) {
        try {
            Class.forName(klass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
