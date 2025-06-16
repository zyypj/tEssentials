package me.zypj.essentials.api.command.models;

import lombok.Data;
import me.zypj.essentials.api.command.enums.SenderType;

import java.lang.reflect.Method;
import java.util.Set;

@Data
public class SubCommandMeta {
    public final String name;
    public final Object handler;
    public final Method method;
    public final String permission;
    public final String usage;
    public final String description;
    public final long cooldown;
    public final Set<SenderType> allowedSenders;
}
