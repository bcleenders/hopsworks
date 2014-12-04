/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.bbc.security.ua;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Ali Gholmai <gholami@pdc.kth.se>
 */
@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {

    /**
     * Ensure the password presented by user during registration is qualified.
     *
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {

        String password = value.toString();

        UIInput uiInputConfirmPassword = (UIInput) component.getAttributes()
                .get("confirmPassword");
        String confirmPassword = uiInputConfirmPassword.getSubmittedValue()
                .toString();

        if (password == null || password.isEmpty() || confirmPassword == null
                || confirmPassword.isEmpty()) {
            FacesMessage facesMsg = new FacesMessage(
                    "PAssword is empty");
            facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMsg);

        }

        if (password.length() < 6) {
            uiInputConfirmPassword.setValid(false);
            FacesMessage facesMsg = new FacesMessage(
                    "Password must be at least 6 characters");
            facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMsg);

        }

        if (isAlphaNumeric(password)) {
            uiInputConfirmPassword.setValid(false);
            FacesMessage facesMsg = new FacesMessage(
                    "password is not alpha-numeric");
            facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMsg);

        }

        if (!password.equals(confirmPassword)) {
            uiInputConfirmPassword.setValid(false);
            FacesMessage facesMsg = new FacesMessage(
                    "Passwords don't match");
            facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMsg);

        }

    }

    /**
     * To check a string if it contains alphanumeric values: MyPassww232¤!#.
     *
     * @param s
     * @return
     */
    public boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z0-9]*$";
        if (s.matches(pattern)) {
            return true;
        }
        return false;
    }
}
