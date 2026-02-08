package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasePage {
    public static final String FLASH_KEY = "flash";
    public static final String FLASH_TYPE_KEY = "flashType";

    private String path;
    private String flash;
    private String flashType;
}
