package microservice.model.validator;

/**
 * @author Maximilian Schirm
 * @created 27.12.2016
 */

public class CSLDummyValidator implements Validator<String> {

    @Override
    public boolean validate(String cslHashToValidate) {
        //Simulating 2% error rate TODO Properly Validate (@Lukas?)
        return (Math.random() > 0.02) ? true : false;
    }

}
