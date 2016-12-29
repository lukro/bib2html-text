package microservice.model.validator;

/**
 * @author Maximilian Schirm
 * created 27.12.2016
 */
public class CSLDummyValidator implements IValidator<String> {

    @Override
    public boolean validate(String cslHashToValidate) {
        return true;
    }

}