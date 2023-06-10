package at.haha007.edencommands.annotations.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CommandList.class)
public @interface Command {
    //acts as prefix if annotated in class
    String value();
}
