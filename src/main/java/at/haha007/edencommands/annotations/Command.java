package at.haha007.edencommands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Command {
    String value();

    //the permission to use this command, or no permission if empty
    String permission() default "";

    //not suitable for LiteralCommandNode, intended for use with ArgumentCommandNode
    String tabCompletes() default "";

    EnumCommandType type() default EnumCommandType.LITERAL;
}
