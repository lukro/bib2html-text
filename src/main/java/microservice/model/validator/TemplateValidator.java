package microservice.model.validator;

import java.io.File;

/**
 * @author Maximilian Schirm, daan
 *         created 27.12.2016
 */
public class TemplateValidator implements IValidator<File> {

    public boolean validate(File templateToValidate) {
        return isValidTemplate(templateToValidate);
    }

    /**
     * @param templateToValidate
     * @return at the moment: always true. implement method to expand functionality.
     */
    private static boolean isValidTemplate(File templateToValidate) {
        return true;
    }

}
