package microservice.model.validator;

/**
 * @author Maximilian
 * on 27.12.2016.
 */
public interface IValidator<V> {

   public boolean validate(V toValidate);

}
