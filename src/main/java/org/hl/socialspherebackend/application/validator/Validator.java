package org.hl.socialspherebackend.application.validator;

public abstract class Validator<T, R> {

    private final int DEFAULT_TEXT_MIN_SIZE = 5;
    private final int DEFAULT_TEXT_MAX_SIZE = 50;
    private final boolean DEFAULT_ACCEPT_BLANK_TEXT = false;
    private final boolean DEFAULT_ACCEPT_WHITESPACE = false;
    private final boolean DEFAULT_ACCEPT_FIRST_CHAR_UPPERCASE = false;

    protected int textMinSize = DEFAULT_TEXT_MIN_SIZE;
    protected int textMaxSize = DEFAULT_TEXT_MAX_SIZE;
    protected boolean acceptBlankText = DEFAULT_ACCEPT_BLANK_TEXT;
    protected boolean acceptWhitespace = DEFAULT_ACCEPT_WHITESPACE;
    protected boolean acceptFirstCharUppercase = DEFAULT_ACCEPT_FIRST_CHAR_UPPERCASE;

    public Validator() {}

    public abstract R validate(T objectToValidate);

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

    public void setAcceptFirstCharUppercase(boolean acceptFirstCharUppercase) {
        this.acceptFirstCharUppercase = acceptFirstCharUppercase;
    }

    public void setDefaultConfiguration() {
        textMinSize = DEFAULT_TEXT_MIN_SIZE;
        textMaxSize = DEFAULT_TEXT_MAX_SIZE;
        acceptBlankText = DEFAULT_ACCEPT_BLANK_TEXT;
        acceptWhitespace = DEFAULT_ACCEPT_WHITESPACE;
        acceptFirstCharUppercase = DEFAULT_ACCEPT_FIRST_CHAR_UPPERCASE;
    }

}
