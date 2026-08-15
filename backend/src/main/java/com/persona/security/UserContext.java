package com.persona.security;

import com.persona.model.User;

public final class UserContext {
    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(User user) {
        if (user == null) HOLDER.remove();
        else HOLDER.set(user);
    }

    public static User get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        User user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
