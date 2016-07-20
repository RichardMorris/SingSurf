/**
 * 
 */
package org.singsurf.singsurf.definitions;


public class Option
	{
		String name;
		OptionType type;
		double  d_val;
		int	i_val;
		String  s_val;
		public Option(String varname,String value)
		{
			name = varname; 
//			this.type = OptionType.getOptionType(type);
			this.s_val = value;
			d_val = 0.0; i_val= 0;
			try
			{
				i_val = Integer.parseInt(value);
				d_val = Double.valueOf(value).doubleValue();
			}
			catch(NumberFormatException e)
			{	// Do really want to ignore this error
				
			}
		}
		public Option(String name,String value,OptionType type)
		{
			this(name,value);
			this.type = type;
		}
		public Option(String line)
		{
			this(LsmpDefReader.getAttribute(line,"name"),
				LsmpDefReader.getAttribute(line,"value"));
		}
		

		public String getName() { return name; }
		public OptionType getType() { return type; }
		public double getDoubleVal() { return d_val; }
		public int getIntegerVal()   { return i_val; }
		public String getStringVal() { return s_val; }
		public String getValue() { return s_val; }
		public boolean getBoolVal()  { return s_val.equals("true"); }
		public void setValue(int val) {
			i_val = val;
			s_val = Integer.toString(val);
		}
		public String toString()
		{
			return "<option name=\"" + name + "\""
				+ " value=\""+s_val+"\">\n";
		}
		public Option duplicate() {
			Option res = new Option(this.name,this.s_val);
			return res;
		}
	}