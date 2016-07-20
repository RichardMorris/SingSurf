package org.singsurf.singsurf.definitions;
import java.io.*;
import java.net.*;
import java.util.*;

import org.nfunk.jep.ParseException;

import jv.object.PsDebug;

/** Contains static methods to parse an XML file containg a set of LsmPDefs. */

public class LsmpDefReader
{
			
	static String getAttribute(String s,String att)
	{
		String search = att+"=\"";
		int nameIndex = s.indexOf(search);
		if(nameIndex == -1)
		{
//			PsDebug.warning("Didn't find atribute "+att+" ("+search+") in string "+s);
			return null;
		}
		else
		{
			int quoteIndex = s.indexOf('\"',nameIndex+att.length()+2);
			if(quoteIndex==-1) return null;
//System.out.println(s+" Att " +att+" nameIndex "+nameIndex+" quoteIndex "+quoteIndex);
			return s.substring(nameIndex+att.length()+2,quoteIndex);
		}
	}

	public LsmpDefReader(String filename) throws IOException,MalformedURLException,SecurityException 
	{
		if(filename.startsWith("http:") || filename.startsWith("file:") )
		{
			URL url = new URL(filename);
			InputStream in = url.openStream();
			br = new BufferedReader(new InputStreamReader(in));
		}
		else
		{
			FileReader f = new FileReader(filename);
			br = new BufferedReader(f);
		}
	}

	public LsmpDefReader(URL url) throws IOException,MalformedURLException,SecurityException 
	{
	    InputStream in = url.openStream();
	    br = new BufferedReader(new InputStreamReader(in));
	}

	public static LsmpDef findDefByName(LsmpDef[] defs,String name)
	{
		for(int i=0;i<defs.length;++i)
			if(defs[i].getName().equals(name)) return defs[i];
		return null;
	}
	
	public static class ProjectInput {
		String project,geometry;

		public ProjectInput(String project,String geometry) {
			this.geometry = geometry;
			this.project = project;
		}
		public String getGeometry() {
			return geometry;
		}
		public String getProject() {
			return project;
		}
	}
	public static class SceneGraph {
		 List<ProjectInput> inputs = new ArrayList<ProjectInput>();
		public boolean add(ProjectInput arg0) {
			return inputs.add(arg0);
		}
		public List<ProjectInput> getInputs() {
			return inputs;
		}
	}
	BufferedReader br=null;
	SceneGraph sg = null;
	public LsmpDefReader(BufferedReader in)
	{
		br=in;
	}
	List<LsmpDef> defs=new ArrayList<LsmpDef>();
	public void read()
	{
		String line;
		StringBuffer buf=null;
		int state=0;
		String lname = null;
		String ltype = null;
		String lopType = null;
		List<DefVariable> vars = null;
		List<Parameter> params = null;
		List<Option> opts = null;
		
		try
		{
		while( (line = br.readLine()) != null)
		{
		  String trim = line.trim();
		  try
		  {
			switch(state)
			{
			case 0:	/* not in a definition */
				if(trim.startsWith("<definition"))
				{
					lname = getAttribute(trim,"name");
					ltype = getAttribute(trim,"type");
					lopType = getAttribute(trim,"opType");
					buf = new StringBuffer();
					vars = new ArrayList<DefVariable>();
					params = new ArrayList<Parameter>();
					opts = new ArrayList<Option>();
					state = 1;
				}
				else if(trim.startsWith("<SceneGraph"))
				{
					sg=new SceneGraph();
					state=2;
				}	
				else if(trim.startsWith("<")) {
					PsDebug.error("LsmpDefs.readDef bad tag '"+ trim+"'");
				}
				break;
			case 1: /* Inside a definition */
				if(trim.equals("</definition>"))
				{
					defs.add(new LsmpDef(lname,ltype,buf.toString(),
						lopType,vars,params,opts) );
					state = 0;
				}
				else if(trim.startsWith("<variable"))
				{
					vars.add(DefVariable.parseTag(trim));
				}
				else if(trim.startsWith("<parameter"))
				{
					params.add(Parameter.parseTag(trim));
				}
				else if(trim.startsWith("<option"))
				{
					opts.add(new Option(trim));
				}
				else if(trim.startsWith("<")) {
					PsDebug.error("LsmpDefs.readDef bad tag '"+ trim+"'");
				}
				else // not a tag must be normal line
					buf.append(line +"\n");
				break;
		  case 2:
			  if(trim.startsWith("<SceneGraph/>"))
				  state=0;
			  else if(trim.startsWith("<input"))
			  {
				  String project=getAttribute(trim,"project");
				  String geometry=getAttribute(trim,"geometry");
				  sg.add(new ProjectInput(project,geometry));
			  }
			  break;
			}
		  }
		  catch(ParseException e) {
				PsDebug.error("LsmpDefs.readDef line '"+ trim+"' "+e.getMessage());
			  state=0;
		  }
		}
		} catch(IOException e)
		{
			PsDebug.error("LsmpDefs.readDef "+ e.getMessage());
		}
	}

	/**
	 * Creates an LsmpDef for a string in XML format.
	 * @return and LsmpDef if string contains exactly 1 definition
	 * 		null  or on error otherwise. 
	 */
	public static LsmpDef createLsmpDef(String def)
	{
		List<LsmpDef> alldefs=null;
		try
		{
			StringReader sr = new StringReader(def);
			BufferedReader br = new BufferedReader(sr);
			LsmpDefReader ldr = new LsmpDefReader(br);
			ldr.read();
			alldefs = ldr.getDefs();
			br.close();
			sr.close();
		}
		catch(IOException e) { return null; }
		if(alldefs == null || alldefs.size() != 1)
			return null;
		else
			return alldefs.get(0);
	}

	public List<LsmpDef> getDefs() {
		return defs;
	}

	/**
	 * @return Returns the sg.
	 */
	public SceneGraph getSceneGraph() {
		return sg;
	}

} // end if class

