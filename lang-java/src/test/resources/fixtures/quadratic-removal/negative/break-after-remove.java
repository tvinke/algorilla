package com.example;

import java.util.List;

public class BreakAfterRemove {
    // The remove() is inside a loop but is always followed by break.
    // The loop removes at most one element — not quadratic.
    public void removeFirstMatch(List<String> roles, String target) {
        for (String role : roles) {
            if (role.equals(target)) {
                roles.remove(role);
                break;
            }
        }
    }
}
