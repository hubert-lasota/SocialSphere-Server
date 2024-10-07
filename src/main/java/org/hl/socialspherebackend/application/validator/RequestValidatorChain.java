package org.hl.socialspherebackend.application.validator;

public abstract class RequestValidatorChain {

    private final RequestValidatorChain next;

    private final int DEFAULT_TEXT_MIN_SIZE = 5;
    private final int DEFAULT_TEXT_MAX_SIZE = 50;
    private final boolean DEFAULT_ACCEPT_BLANK_TEXT = false;
    private final boolean DEFAULT_ACCEPT_WHITESPACE = false;
    private final boolean DEFAULT_FORCE_FIRST_CHAR_UPPERCASE = false;
    private final boolean DEFAULT_NULLABLE = false;

    protected int textMinSize = DEFAULT_TEXT_MIN_SIZE;
    protected int textMaxSize = DEFAULT_TEXT_MAX_SIZE;
    protected boolean acceptBlankText = DEFAULT_ACCEPT_BLANK_TEXT;
    protected boolean acceptWhitespace = DEFAULT_ACCEPT_WHITESPACE;
    protected boolean forceFirstCharUppercase = DEFAULT_FORCE_FIRST_CHAR_UPPERCASE;
    protected boolean nullable = DEFAULT_NULLABLE;

    public RequestValidatorChain(RequestValidatorChain next) {
        this.next = next;
    }

    public RequestValidateResult validate(Object request) {
        if(isRequestValidInstance(request)) {
            return doValidate(request);
        }

        if(next == null) {
            throw new RuntimeException("There is no RequestValidator for this request type=%s"
                    .formatted(request.getClass().toString()));
        }

        return next.validate(request);
    }

    protected abstract RequestValidateResult doValidate(Object request);

    protected abstract boolean isRequestValidInstance(Object request);

    protected boolean containsWhitespace(String string) {
        for (char c : string.toCharArray()) {
            if(Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    public void setTextMinSize(int textMinSize) {
        this.textMinSize = textMinSize;
    }

    public void setTextMaxSize(int textMaxSize) {
        this.textMaxSize = textMaxSize;
    }

    public void setAcceptBlankText(boolean acceptBlankText) {
        this.acceptBlankText = acceptBlankText;
    }

    public void setAcceptWhitespace(boolean acceptWhitespace) {
        this.acceptWhitespace = acceptWhitespace;
    }

    public void setForceFirstCharUppercase(boolean forceFirstCharUppercase) {
        this.forceFirstCharUppercase = forceFirstCharUppercase;
    }

    public void setDefaultConfiguration() {
        textMinSize = DEFAULT_TEXT_MIN_SIZE;
        textMaxSize = DEFAULT_TEXT_MAX_SIZE;
        acceptBlankText = DEFAULT_ACCEPT_BLANK_TEXT;
        acceptWhitespace = DEFAULT_ACCEPT_WHITESPACE;
        forceFirstCharUppercase = DEFAULT_FORCE_FIRST_CHAR_UPPERCASE;
    }

}
