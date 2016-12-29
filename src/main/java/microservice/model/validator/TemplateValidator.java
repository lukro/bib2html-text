package microservice.model.validator;

/**
 * @author Maximilian Schirm
 * created 27.12.2016
 */
public class TemplateValidator implements IValidator<String> {

    @Override
    public boolean validate(String toValidate) {
        return true;
    }

}
