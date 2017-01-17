package microservice.model.validator;

import java.io.File;

/**
 * @author Maximilian Schirm, daan
 *         created 27.12.2016
 */
public class CslValidator implements IValidator<File> {

    public boolean validate(File cslFileToValidate) {
        return isValidCslFile(cslFileToValidate);
    }

    /**
     * @param cslFileToValidate
     * @return at the moment: always true. implement method to expand functionality.
     */
    private static boolean isValidCslFile(File cslFileToValidate) {
        return true;
    }

}