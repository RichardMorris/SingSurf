/*
Created 5 Feb 2007 - Richard Morris
*/
package org.singsurf.singsurf;

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import jv.object.PsObject;

public class Fractometer  {

	/** The value of the control */
	double value;
	/** The number of decimal places to display */
	int		decimalPlaces=1;
	double minVal = -Double.MAX_VALUE;
	double maxVal = Double.MAX_VALUE;
	
	FractPanel fp=null;
	TextField tf = new TextField("0.0");
	NumberFormat nf = null;
	PsObject parent=null;
	PopupMenu pm =null;
	Button menuBut = null;
	/**
	 * Inner class for the displayable component.
	 * @author Richard Morris
	 *
	 */
	public class FractPanel extends Panel  implements ActionListener {
		private static final long serialVersionUID = 1L;

		public FractPanel() {
			GridBagLayout gridbag = new GridBagLayout();
	        GridBagConstraints c = new GridBagConstraints();
	        c.anchor = GridBagConstraints.NORTHWEST;
	        c.fill = GridBagConstraints.BOTH;
	        c.gridheight = 1;
	        c.gridwidth = 1;
	        c.weightx = 0.1;
	        c.weighty = 1;
	        c.gridx = 0;
	        c.gridy = 0;
	        c.ipadx = 0;
	        c.ipady = 1;
	        c.insets = new Insets(0,0,0,0);
	        this.setLayout(gridbag);
			Button bmm = new RepeatButton("<<");
			bmm.setBackground(Color.BLUE);
			bmm.setPreferredSize(new Dimension(25,25));
			bmm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					adjustValue(-10);
				}

			});
			Button bm = new RepeatButton("<");
            bm.setPreferredSize(new Dimension(25,25));
			bm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					adjustValue(-1);
				}});
			Button bp = new RepeatButton(">");
            bp.setPreferredSize(new Dimension(25,25));
			bp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					adjustValue(1);
				}});
			Button bpp = new RepeatButton(">>");
            bpp.setPreferredSize(new Dimension(25,25));
			bpp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					adjustValue(10);
				}});
			tf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setValue(tf.getText());
				}});
			
			this.add(bmm,c);
			c.gridx++;
			this.add(bm,c);
			c.gridx++; c.weightx = 0.9;
			this.add(tf,c);
			c.gridx++; c.weightx = 0.1;
			this.add(bp,c);
			c.gridx++;
			this.add(bpp,c);
			c.gridx++;
			
			String[] labels = new String[]{"Zero","0","1","2","3","4","5","6"};
			pm = new PopupMenu();
			for(int i=0;i<labels.length;++i) {
				MenuItem mi = new MenuItem(labels[i]);
				mi.setActionCommand(labels[i]);
				mi.addActionListener(this);
				pm.add(mi);
			}
			this.add(pm);
			menuBut = new Button("c");
            menuBut.setPreferredSize(new Dimension(25,25));
			menuBut.addMouseListener(new MouseListener(){
				public void mousePressed(MouseEvent arg0) {
					pm.show(FractPanel.this,menuBut.getX(),menuBut.getY());
					arg0.consume();
				}

				public void mouseClicked(MouseEvent arg0) {}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mouseReleased(MouseEvent arg0) {/*System.out.println("c release");*/}
				});
			this.add(menuBut,c);
		}
		public void actionPerformed(ActionEvent event) {
			String command = event.getActionCommand();
			if(command.equals("Zero")) {
				setValue(0);
				if(parent!=null) parent.update(this);
				}
			else
				setDecimalPlaces(Integer.parseInt(command));
		}

	}

	public Fractometer(double value) {
		fp = new FractPanel();
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(this.decimalPlaces);
		nf.setMinimumFractionDigits(this.decimalPlaces);
		setValue(value);
	}

	Container outerPanel;
	public void addComponents(Container panel, GridBagConstraints c) {
		outerPanel = panel;
	}

	private void adjustValue(int amount) {
		double scale = Math.pow(10,-decimalPlaces);
		setValue(this.value+scale * amount);
		if(parent!=null) parent.update(this);
	}
	private double setValue(String text) {
		try {
			Number n = nf.parse(text);
			setValue(n.doubleValue());
			if(parent!=null) parent.update(this);
		} catch (ParseException e) {
			displayValue();
		}
		return value;
	}

	public double setValue(double val) {
		if(val < minVal) val = minVal;
		if(val > maxVal) val = maxVal;
		this.value = val;
		displayValue();
		return value;
	}

	private void displayValue() {
		tf.setText(nf.format(this.value));
	}
	
	
	public int getDecimalPlaces() {
		return decimalPlaces;
	}
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
		nf.setMaximumFractionDigits(this.decimalPlaces);
		nf.setMinimumFractionDigits(this.decimalPlaces);
		displayValue();
	}
	public double getMaxVal() {
		return maxVal;
	}
	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
		setValue(this.value);
	}
	public double getMinVal() {
		return minVal;
	}
	public void setMinVal(double minVal) {
		this.minVal = minVal;
		setValue(this.value);
	}
	public double getValue() {
		return value;
	}
	public Panel getPanel() {
		return this.fp;
	}
	
	public void setParent(PsObject parent) {
		this.parent = parent;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Fract main");
		Frame frame	= new Frame("parameters");
		frame.setBounds(new Rectangle(395, 5, 630, 600));
//			frame.setBounds(new Rectangle(100, 5, 830, 550));
		frame.setLayout(new GridLayout(4,1));
		Fractometer f1 = new Fractometer(1.0);
		frame.add(f1.getPanel());
		Fractometer f2 = new Fractometer(1.0);
		frame.add(f2.getPanel());
		Fractometer f3 = new Fractometer(1.0);
		frame.add(f3.getPanel());
		Fractometer f4 = new Fractometer(1.0);
		frame.add(f4.getPanel());
		Button b = new Button("Press me");
		b.addMouseMotionListener(new MouseMotionListener(){

			public void mouseDragged(MouseEvent arg0) {
				System.out.println("Mouse dragged"+System.currentTimeMillis());
				
			}

			public void mouseMoved(MouseEvent arg0) {
				System.out.println("Mouse moved"+System.currentTimeMillis());
				
			}});
		frame.add(b);
		frame.setVisible(true);
	}
}
