package microservice.model.validator;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 27.12.2016
 */

public class TemplateValidator implements Validator<String> {

    @Override
    public boolean validate(String toValidate) {
        //Simulating 2% error rate TODO Properly Validate (@Lukas?)
        return (Math.random() > 0.02) ? true : false;
    }

}
