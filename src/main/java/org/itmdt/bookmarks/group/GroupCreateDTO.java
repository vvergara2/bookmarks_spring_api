package org.itmdt.bookmarks.group;

import javax.validation.constraints.NotBlank;

public class GroupCreateDTO {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
