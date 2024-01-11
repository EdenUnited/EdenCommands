package at.haha007.edencommands.annotations;

import at.haha007.edencommands.annotations.annotations.*;
import com.google.auto.service.AutoService;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@AutoService(javax.annotation.processing.Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("at.haha007.edencommands.annotations.annotations.Command")
public class SyntaxCheckerProcessor extends AbstractProcessor {
    private static final String COMMAND_CONTEXT_CLASS_PATH = "at.haha007.edencommands.CommandContext";
    private static final String WHITESPACE_REGEX = ".*\\s.*";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Command.class).forEach(this::checkCommandAnnotation);
        roundEnv.getElementsAnnotatedWith(CommandArgument.class).forEach(this::checkCommandArgumentAnnotation);
        roundEnv.getElementsAnnotatedWith(CommandRequirement.class).forEach(this::checkCommandRequirementAnnotation);
        roundEnv.getElementsAnnotatedWith(TabCompleter.class).forEach(this::checkTabCompleterAnnotation);
        roundEnv.getElementsAnnotatedWith(SyncCommand.class).forEach(this::checkSyncCommandAnnotation);
        roundEnv.getElementsAnnotatedWith(DefaultExecutor.class).forEach(this::checkDefaultExecutor);
        return false;
    }

    private void checkCommandAnnotation(Element element) {
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
            tree.add(List.of(value), null, c -> {
            }, false);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Command annotation value has invalid syntax", e);
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
        if (!method.getParameters().get(0).asType().toString().equals(COMMAND_CONTEXT_CLASS_PATH)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "CommandExecutors have to have only CommandContext as parameter", element);
        }
    }

    private void checkCommandArgumentAnnotation(Element element) {
        CommandArgument command = element.getAnnotation(CommandArgument.class);
        if (command == null) {
            return;
        }
        if (command.value().isBlank()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Argument key cannot be empty", element);
            return;
        }
        if (command.value().matches(WHITESPACE_REGEX)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Argument key must not contain whitespace", element);
            return;
        }

        //has to return void and have CommandContext as parameter
        if (element.getKind() == ElementKind.FIELD) {
            VariableElement field = (VariableElement) element;
            ReferenceType type = (ReferenceType) field.asType();
            if (!type.toString().startsWith("at.haha007.edencommands.annotations.annotations.ArgumentParser")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Argument has to be ArgumentParser but is " + type, element);
                return;
            }
        }
        if (element.getKind() == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) element;
            String returnType = method.getReturnType().toString();
            returnType = returnType.substring(0, returnType.indexOf("<"));

            if (!returnType.startsWith("at.haha007.edencommands.argument.ParsedArgument")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments have to return ParsedArgument but is returning " + method.getReturnType().toString(), element);
                return;
            }
            if (method.getParameters().size() != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Too many method parameters. Arguments must have only CommandContext as parameter", element);
                return;
            }
            if (!method.getParameters().get(0).asType().toString().equals(COMMAND_CONTEXT_CLASS_PATH)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Wrong method parameter. Arguments must have to have only CommandContext as parameter", element);
            }
        }
    }

    private void checkTabCompleterAnnotation(Element element) {
        TabCompleter command = element.getAnnotation(TabCompleter.class);
        if (command == null) {
            return;
        }
        if (command.value().isBlank()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "TabCompleter key cannot be empty", element);
            return;
        }
        if (command.value().matches(WHITESPACE_REGEX)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "TabCompleter key must not contain whitespace", element);
            return;
        }

        //has to return void and have CommandContext as parameter
        if (element.getKind() == ElementKind.FIELD) {
            VariableElement field = (VariableElement) element;
            ReferenceType type = (ReferenceType) field.asType();
            if (!type.toString().equals("at.haha007.edencommands.TabCompleter")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments has to be ArgumentParser but is " + type, element);
                return;
            }
        }
        if (element.getKind() == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) element;
            String returnType = method.getReturnType().toString();

            if (!returnType.equals("java.util.List<com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion>")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments have to return List<AsyncTabCompleteEvent.Completion> but is returning " + returnType, element);
                return;
            }
            if (method.getParameters().size() != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments must have only CommandContext as parameter", element);
                return;
            }
            if (!method.getParameters().get(0).asType().toString().equals(COMMAND_CONTEXT_CLASS_PATH)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments must have to have only CommandContext as parameter", element);
            }
        }
    }

    private void checkCommandRequirementAnnotation(Element element) {
        CommandRequirement annotation = element.getAnnotation(CommandRequirement.class);
        if (annotation == null) {
            return;
        }
        if (annotation.value().isBlank()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Argument key cannot be empty", element);
            return;
        }
        if (annotation.value().matches(WHITESPACE_REGEX)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Argument key must not contain whitespace", element);
            return;
        }

        //commands get checked in another method
        if (element.getAnnotation(Command.class) != null)
            return;


        //has to return void and have CommandContext as parameter
        if (element.getKind() == ElementKind.FIELD) {
            VariableElement field = (VariableElement) element;
            ReferenceType type = (ReferenceType) field.asType();
            if (!type.toString().equals("at.haha007.edencommands.Requirement")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Requirement has to be Requirement but is " + type, element);
                return;
            }
        }
        if (element.getKind() == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) element;
            String returnType = method.getReturnType().toString();
            returnType = returnType.substring(0, returnType.indexOf("<"));

            if (!returnType.equals("boolean")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments have to return ParsedArgument but is returning " + method.getReturnType().toString(), element);
                return;
            }
            if (method.getParameters().size() != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments must have only CommandContext as parameter", element);
                return;
            }
            if (!method.getParameters().get(0).asType().toString().equals(COMMAND_CONTEXT_CLASS_PATH)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arguments must have to have only CommandContext as parameter", element);
            }
        }
    }

    private void checkSyncCommandAnnotation(Element element) {
        SyncCommand annotation = element.getAnnotation(SyncCommand.class);
        if (annotation == null) {
            return;
        }

        if (element.getKind() == ElementKind.CLASS) {
            if (element.getEnclosedElements().stream().anyMatch(e -> e.getAnnotation(Command.class) != null))
                return;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "SyncCommand must also be annotated with @Command", element);
            return;
        }
        if (element.getAnnotation(Command.class) != null)
            return;
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "SyncCommand must also be annotated with @Command", element);
    }

    private void checkDefaultExecutor(Element element) {
        DefaultExecutor annotation = element.getAnnotation(DefaultExecutor.class);
        if (annotation == null) {
            return;
        }

        if (element.getKind() == ElementKind.CLASS) {
            if (element.getEnclosedElements().stream().anyMatch(e -> e.getAnnotation(Command.class) != null))
                return;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "DefaultExecutor must also be annotated with @Command", element);
            return;
        }
        if (element.getAnnotation(Command.class) != null)
            return;
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "DefaultExecutor must also be annotated with @Command", element);
    }

}
