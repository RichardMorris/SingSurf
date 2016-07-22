package org.singsurf.singsurf.jep;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

/**
 * Base class for functions that operate on arrays such as Average, MinMax, 
 * and VSum. The shared functionality such as array flattening is implemented
 * here. In the run() method, before calling calc(), Vectors and nested Vectors 
 * are flattened so <code>1,[2,[3, 4]]</code> becomes <code>[1, 2, 3, 4]</code>
 * which is passed in to the calc method as a List&lt;Object&gt;.
 * @since 3.4.0
 */
public abstract class ArrayFunctionBase extends PostfixMathCommand
{    
    /** How to respond to a zero length array as argument */
    public enum ZeroLengthErrorBehaviour {
        /** Signals that an error is thrown */ EXCEPTION, 
        /** Signals that NaN is returned */    NAN }
    
    /** The zero array length setting */
    ZeroLengthErrorBehaviour zeroLengthErrorBehaviour = ZeroLengthErrorBehaviour.EXCEPTION;
    
    /**
     * Default the number of parameters to any number of params (-1).
     */
    public ArrayFunctionBase()
    {
        numberOfParameters = -1;
    }

    /**
     * Must have one or more parameter
     */
    @Override
    public boolean checkNumberOfParameters(int n) {
        return n>0;
    }

    /**
     * Calls the calc method after concatenating all elements into list.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void run(Stack stack) throws ParseException
    {
         // if only single parameter of type vector, call function on it
        // otherwise, wrap all parameters into a vector
        java.util.List<Object> v = new ArrayList<Object>();
        // wrap all arguments into a vector
        for (int i=0; i<curNumberOfParameters; i++) {
             addToArray(v, stack.pop());
        }

        Object res = calc(v);
        stack.push(res);
    }

    /**
     * Adds a value <code>val</code> to the array <code>l</code>. This method
     * flattens Vectors and nested Vectors so <code>1,[2,[3, 4]]</code> becomes 
     * <code>[1, 2, 3, 4]</code>.
     * @param l The list to which to add <code>a</code>.
     * @param val The value to be added (can be a Vector).
     */
    protected void addToArray(java.util.List<Object> l,Object val) {
        if (val instanceof List<?>) {
            for (Object o:(List<?>) val) 
                addToArray(l,o);
        }
        else
            l.add(val);
    }
    
    /**
     * Abstract method for performing the array calculation.
     * @param l The list to operate on. Note this is in reverse order of the arguments of the function.
     * @return The result of the calculation.
     * @throws ParseException if the calculation cannot be performed
     */
    protected abstract Object calc(List<Object> l) throws ParseException;

    protected void throwAtLeastOneExcep() throws ParseException {
        throw new ParseException("At lest one argument is required"); 
    }

    public ZeroLengthErrorBehaviour getZeroLengthErrorBehaviour() {
        return zeroLengthErrorBehaviour;
    }

    /**
     * Sets how to respond to arguments with zero length arrays.
     * Either an Exception is thrown or NaN is returned.
     * @param zeroLengthErrorBehaviour either EXCEPTION or NAN
     */
    public void setZeroLengthErrorBehaviour(
            ZeroLengthErrorBehaviour zeroLengthErrorBehaviour) {
        this.zeroLengthErrorBehaviour = zeroLengthErrorBehaviour;
    }

}
