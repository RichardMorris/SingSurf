package org.singsurf.singsurf.jep;

/**
 * Wrapper class to indicate errors during evaluation
 * @author rich
 *
 */
public class EvaluationException extends Exception {
    private static final long serialVersionUID = 1L;

    public EvaluationException(Exception e) {
        super(e);
    }

    public EvaluationException(String s) {
        super(s);
    }

}
