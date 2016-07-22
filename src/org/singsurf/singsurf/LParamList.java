/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jv.object.PsObject;
import jv.object.PsPanel;

import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.definitions.Parameter;

/**
 * Holds a list of interactive parameters.
 * To save unnecessary deletion and creation of parameters
 * use
 * <pre>
 * LParamList lpl = new LParamList(parent);
 * ...
 * lpl.reset();
 * addParameter(a,1);
 * addParameter(b,2);
 * ...
 * lpl.rebuild();
 * </pre>
 * 
 * @author Rich Morris
 * Created on 31-Mar-2005
 */

public class LParamList extends PsPanel implements ComponentListener {
    private static final long serialVersionUID = 1L;

    private boolean changed;
	PsObject parent;
	List<PuParameter> v = new ArrayList<PuParameter>();
	ScrollPane pane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
	PsPanel inner = new PsPanel();
	GridBagConstraints gbc=null;
	public LParamList(AbstractClient parent) {
		this.parent = parent;
		this.add(pane);
		pane.add(inner);
	//	this.add(inner);
	//	inner.setBackground(Color.cyan);
		inner.setBorderType(PsPanel.BORDER_NONE);
		inner.setInsetSizeVertical(1);
		this.addComponentListener(this);
		GridBagLayout gridbag = new GridBagLayout();
        inner.setLayout(gridbag);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        //gbc.ipadx = 1;
        gbc.ipady = 1;

		//pane.setBounds(this.getBounds());
	}
	@SuppressWarnings("unused")
    private LParamList() {};
	
	public PuParameter addParameter(Parameter p) {
		for(int i=0;i<v.size();++i) {
			PuParameter q = v.get(i);
			int res = p.getName().compareTo(q.getName());
			if(res<0) {
				PuParameter r = new PuParameter(parent,p);
				r.setRef(1);
				v.add(i,r);
				changed = true;
				return r;
			}
			if(res==0) {
			        q.setVal(p.getVal());
				q.setRef(1);
				return q;
			}
		}
		PuParameter r = new PuParameter(parent,p);
		r.setRef(1);
		v.add(r);
		changed = true;
		return r;
	}
	public PuParameter addParameter(Parameter p,double val) {
		PuParameter q = addParameter(p);
		if(q==null) return null;
		q.setVal(val);
		return q;
	}
	public void reset() {
		for(PuParameter p:v) {
			p.setRef(-1);
		}
		changed = false;
	}
	public void rebuild() {
		Iterator<PuParameter> it = v.iterator();
		while(it.hasNext()) {
			PuParameter p = it.next();
			if(p.getRef()<0) {
				it.remove();
				changed = true;
			}
		}
		if(changed) {
			inner.removeAll();
	        gbc.gridx = 0;
	        gbc.gridy = 0;
			gbc.gridwidth = 1;
			
			for(PuParameter p:v) {
				//p.control.addComponents(inner,gbc);
				inner.add(p.getLabel(),gbc);
				gbc.gridx++;
				inner.add(p.getControlPanel(),gbc);
				gbc.gridx=0;
				gbc.gridy++;
			}
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			gbc.weighty = 1;
			inner.add(new Panel(),gbc);
			inner.doLayout();
		}
	}
	@Override
    public boolean update(Object arg) {
		System.out.println("LParamList.update("+arg+")");
		return parent.update(arg);
	}
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void componentResized(ComponentEvent arg0) {
//		System.out.println("Resized");
//		System.out.println("inner: "+inner.getBounds());
//		System.out.println("inner pref "+inner.getPreferredSize());
//		System.out.println("inner insets "+inner.getInsets());
//		System.out.println("pane: "+pane.getBounds());
//		System.out.println("this: "+this.getBounds());
//		System.out.println("par: "+this.getParent().getBounds());
//		System.out.println("parpar: "+this.getParent().getParent().getBounds());
		this.pane.setBounds(this.getParent().getBounds());
		int h = this.inner.getHeight();
		int w = (int) this.pane.getViewportSize().getWidth();
		//int w = this.pane.getWidth()-this.pane.getVScrollbarWidth()*2;
		//this.inner.setSize(w,h);
		//this.inner.setPreferredSize(new Dimension(w,h));
		this.inner.setSize(w,h);
		this.pane.doLayout();
/**		this.inner.invalidate();
		System.out.println("inner: "+inner.getBounds());
		for(PuParameter p:v) {
			Panel pan = p.getControlPanel();
			if(pan!=null) {
				h = pan.getHeight();
				//pan.setPreferredSize(w,h);
				pan.setSize(w,h);
				pan.invalidate();
				System.out.println("pan: "+pan.getBounds());
			}
			//p.g
		}
**/

	}
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}