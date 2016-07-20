/*
Created 23 May 2011 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.util.List;

import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomStore;

public interface ClientFactory {
    /** Create a new instance with a given def
     * 
     * @param name Name of client
     * @param def definition of surface
     * @param store GeomStore object
     * @return a new AbstractClient instance
     */
    public AbstractClient newInstance(String name,LsmpDef def,GeomStore store);
    
    /**
     * Create a new instance with no predefined def
     * @param name Name of client
     * @param defs list of definitions
     * @param store GeomStore object
     * @return  a new AbstractClient instance
     */
    public AbstractClient newInstance(String name,List<LsmpDef> defs,GeomStore store);
}
