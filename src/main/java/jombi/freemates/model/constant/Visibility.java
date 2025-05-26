package jombi.freemates.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Visibility {
    PUBLIC("공개"),
    PRIVATE("비공개");

    private final String description;

}
