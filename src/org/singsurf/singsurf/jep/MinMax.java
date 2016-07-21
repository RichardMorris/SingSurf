package org.singsurf.singsurf.jep;

import java.util.List;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.Comparative;

/**
 * Minimum and Maximum functions. Initialize with true for minimum and false
 * for maximum.
 * Since Jep 3.4 this function will flatten its arguments so <code>min([1,[2,3]])</code> 
 * will be  1.
 * @since 3.1.0
 */
public class MinMax extends ArrayFunctionBase
{
    private static final long serialVersionUID = 310L;

    /**
     * Used to compare greater than or less than between objects.
     */
    protected Comparative comp;
    protected boolean isMin;

    /**
     * Constructor.
     * @param isMin set to true for Minimum, false for Maximum
     */
    public MinMax(boolean isMin)
    {
        this.isMin = isMin;
        this.comp = new Comparative(isMin?Comparative.LT:Comparative.GT);
    }

    
    /**
     * Constructor allowing for a different Comparative object.
     * The {@link Comparative#compare(Object, Object)} method will be used to compare elements.
     * @param comp a Comparative object or subclass implemented less than or greater than.
     * @since 3.4.0
     */
    public MinMax(Comparative comp) {
        this.comp = comp;
        numberOfParameters = -1;
    }


    @Override
    protected Object calc(List v) throws ParseException {
         return minmax(v);
    }

    public Object minmax(List<Object> vals) throws ParseException {
        if(vals.size()==0) {
            switch(this.zeroLengthErrorBehaviour) {
            case EXCEPTION: throwAtLeastOneExcep();
                break;
            case NAN: return Double.NaN;
            }
        }

        int i = 1;

        Object extreme = vals.get(0);
        // loop through elements
        while (i < vals.size()) {
            if(isMin) {
                if (comp.lt(vals.get(i), extreme)) {
                    extreme = vals.get(i);
                }
            } else {
                if (comp.gt(vals.get(i), extreme)) {
                    extreme = vals.get(i);
                }
            }
            i++;
        }
        return extreme;
    }

    /**
     * Return the Comparative objected used to order the objects.
     * @return the Comparative object
     * @since 3.4.0
     */
    public Comparative getComp() {
        return comp;
    }
}
