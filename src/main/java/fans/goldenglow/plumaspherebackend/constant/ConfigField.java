package fans.goldenglow.plumaspherebackend.constant;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum ConfigField {
    INITIALIZED(true),
    BLOG_TITLE(true),
    BLOG_SUBTITLE(true),
    PAGE_SIZE(true),
    CONFIG_VERSION(false);

    private final boolean isOpenToPublic;

    ConfigField() {
        this(false);
    }

    ConfigField(boolean isOpenToPublic) {
        this.isOpenToPublic = isOpenToPublic;
    }

    public static Optional<ConfigField> tryParse(String name) {
        try {
            return Optional.of(valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

}
