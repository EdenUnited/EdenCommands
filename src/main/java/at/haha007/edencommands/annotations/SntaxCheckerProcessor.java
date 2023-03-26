package at.haha007.edencommands.annotations;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.util.Set;

@AutoService(javax.annotation.processing.Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("at.haha007.edencommands.annotations.Command")
public class SntaxCheckerProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Command.class).forEach(this::checkAnnotatedElement);
        return false;
    }

    private void checkAnnotatedElement(Element element) {
        Command command = element.getAnnotation(Command.class);
        if (command == null) {
            return;
        }
        if (command.value().isBlank()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Command annotation value cannot be empty", element);
            return;
        }
        //get value and pass it into a CommandTree, catch errors
        String value = command.value();
        CommandTree tree = CommandTree.root();
        try {
            tree.add(value, c -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Command annotation value has invalid syntax", element);
            return;
        }


        //has to return void and have CommandContext as parameter
        if (element.getKind() != ElementKind.METHOD) {
            return;
        }
        ExecutableElement method = (ExecutableElement) element;
        if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "CommandExecutors have to return void", element);
            return;
        }
        if (method.getParameters().size() != 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "CommandExecutors have to have only CommandContext as parameter", element);
            return;
        }
        if (!method.getParameters().get(0).asType().toString().equals("at.haha007.edencommands.CommandContext")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "CommandExecutors have to have only CommandContext as parameter", element);
        }
    }

}
