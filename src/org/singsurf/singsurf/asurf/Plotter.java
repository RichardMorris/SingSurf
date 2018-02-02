package org.singsurf.singsurf.asurf;

/**
 * Interface to define the plotting of a calculated surface.
 */
public interface Plotter {

    /**
     * Plot all the geometry inside a box.
     * @param box
     */
    void plot_box(Box_info box);

    /**
     * Call before plotting
     */
    void initPlotter();

    /**
     * Call after plotting. 
     */
    void finiPlotter();

    /**
     * Remove all tempory data which might have been stored by the plotter
     */
    void clear();

}