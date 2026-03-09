package dev.goldmensch.propane.spec.processor.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
class OpenJDKJavaDocReader implements JavaDocReader {

    private final Elements elements;
    private final DocTrees trees;

    OpenJDKJavaDocReader(ProcessingEnvironment environment) {
        this.trees = DocTrees.instance(environment);
        this.elements = environment.getElementUtils();
    }

    public Comment get(Element element) {
        TreePath path = trees.getPath(element);
        DocCommentTree comment = trees.getDocCommentTree(path);
        if (comment == null) return EMPTY_COMMENT;

        DocRefScanner scanner = new DocRefScanner(element, path);
        comment.accept(scanner, null);

        return new Comment(trees.getDocComment(path), scanner.references);
    }

    // TODO: revisit when writing tests
    class DocRefScanner extends DocTreeScanner<Void, Void> {

        private final List<TypeElement> references = new ArrayList<>();

        private final Element element;
        private final CompilationUnitTree unit;

        DocRefScanner(Element element, TreePath path) {
            this.element = element;
            this.unit = path.getCompilationUnit();
        }

        @Override
        public Void visitReference(ReferenceTree node, Void unused) {
            String signature = node.getSignature();

            // remove member reference (#method)
            signature = signature.replaceAll("#.*", "");

            // remove generics
            signature = signature.replaceAll("<.*>", "");

            if (signature.isEmpty()) return null;

            resolve(signature).ifPresent(references::add);

            return null;
        }

        private Optional<TypeElement> resolve(String name) {
            // fully qualified - don't need to import
            TypeElement t = elements.getTypeElement(name);
            if (t != null) return Optional.empty();

            // split nested parts
            String simple = name.split("\\.", 2)[0];

            // explicit imports
            for (ImportTree imp : unit.getImports()) {
                if (imp.isStatic()) continue;

                String q = imp.getQualifiedIdentifier().toString();

                if (q.endsWith("." + simple)) {

                    String resolved = q;
                    if (name.contains(".")) {
                        resolved += name.substring(simple.length());
                    }


                    t = elements.getTypeElement(resolved);
                    if (t != null) return Optional.of(t);
                }
            }

            // wildcard imports
            for (ImportTree imp : unit.getImports()) {
                if (imp.isStatic()) continue;

                String q = imp.getQualifiedIdentifier().toString();

                if (q.endsWith(".*")) {
                    String pkg = q.substring(0, q.length() - 2);

                    t = elements.getTypeElement(pkg + "." + name);
                    if (t != null) return Optional.of(t);
                }
            }

            // same package
            String pkg = elements.getPackageOf(element)
                    .getQualifiedName()
                    .toString();

            if (!pkg.isEmpty()) {
                t = elements.getTypeElement(pkg + "." + name);
                if (t != null) return Optional.of(t);
            }

            // 5️⃣ java.lang
            t = elements.getTypeElement("java.lang." + name);
            if (t != null) return Optional.of(t);

            return Optional.empty();
        }
    }
}
