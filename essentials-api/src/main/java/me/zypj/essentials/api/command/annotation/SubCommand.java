package me.zypj.essentials.api.command.annotation;

import me.zypj.essentials.api.command.enums.SenderType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {
    String parent();

    String name();

    String permission() default "";

    String usage() default "";

    String description() default "";

    long cooldown() default 0L;

    SenderType[] sender() default {SenderType.ALL};
}
