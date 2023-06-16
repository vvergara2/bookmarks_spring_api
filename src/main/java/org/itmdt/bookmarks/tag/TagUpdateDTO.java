package org.itmdt.bookmarks.tag;

import javax.validation.constraints.NotBlank;
import java.util.Date;

public class TagUpdateDTO {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
