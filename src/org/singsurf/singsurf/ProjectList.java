/*
Created 23 May 2011 - Richard Morris
*/
package org.singsurf.singsurf;

import java.awt.Component;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.singsurf.singsurf.clients.AbstractClient;

public class ProjectList  {
    private static final long serialVersionUID = 1L;
    java.awt.List awtList = new java.awt.List();
    List<AbstractClient> projects = new ArrayList<AbstractClient>();

    
    public void add(String item) {
        awtList.add(item);
        projects.add(null);
    }

    public AbstractClient getSelectedProject() {
        int i = awtList.getSelectedIndex();
        if(i<0) return null;
        return projects.get(i);
    }

    public void add(AbstractClient newClient) {
        awtList.add(newClient.getName());
        projects.add(newClient);
        
    }

    public AbstractClient getProject(int i) {
        return projects.get(i);
    }

    public List<AbstractClient> getProjects() {
        return projects;
    }
    
    public void remove(int position) {
        awtList.remove(position);
        projects.remove(position);
    }

    public synchronized void replaceItem(String newValue, int index) {
        awtList.replaceItem(newValue, index);
    }

    public int getItemCount() {
        return awtList.getItemCount();
    }

    public String getItem(int index) {
        return awtList.getItem(index);
    }

    public void select(int i) {
        awtList.select(i);
    }

    public String getSelectedItem() {
        return awtList.getSelectedItem();
    }

    public int getSelectedIndex() {
        return awtList.getSelectedIndex();
    }

    public Component getAwtList() {
        return awtList;
    }

    public void addItemListener(ItemListener listener) {
        awtList.addItemListener(listener);
    }

    public void addMouseListener(MouseListener listener) {
        awtList.addMouseListener(listener);
    }

    @Override
    public String toString() {
        String res = Arrays.toString(awtList.getItems()) + projects.toString();
        return res;
    } 
    
     
    
    
}
