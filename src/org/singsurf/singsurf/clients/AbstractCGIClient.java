/*
Created 12-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.loader.PgJvxLoader;
import jv.object.PsDebug;
import jv.project.PgJvxSrc;
import jv.project.PvGeometryIf;

import org.singsurf.singsurf.CgiStringMakerI;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.LmsPointSetMaterial;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.geometries.GeomStore;

/**
 * @author Richard Morris
 *
 */
public abstract class AbstractCGIClient extends AbstractClient implements CgiStringMakerI {
	private static final long serialVersionUID = -3454556610446028976L;

	//******************* Geometry related fields ***********
	/** Contains the geometry of the surface **/
	private	PgElementSet	m_geom;
	/** Geometry for line set **/
	private	PgPolygonSet	m_line_geom;
	/** Geometry for isolated points **/
	private	PgPointSet	m_point_geom;

	/** The model loaded by default (defaultsurf.jvx.gz). */
	private	String		m_defModelName = null;

	/** A unique number for the geoms **/
	private		int		uniqueNum = 0;

	//****************** Server related fields *************
	/** The geometry server is an executable. */
	public static final int EXEC_SERVER=1;
	/** The geometry server is a CGI script. */
	public static final int CGI_SERVER=2;
	/** No geometry server is needed. */
	public static final int NO_SERVER=0;
	/** The type of geometry server. */
	public int serverType = NO_SERVER;
    /** the URL/filename of server */
	protected String server=null;


	/**
	 * The name of the executable. 
	 */
//	protected String		s_asurfExec = null;
	/** dir portion of s_asurfURL */
//	protected String	s_asurfURL_dir;
//	protected String	s_asurfURL_exe;
//	protected String	s_asurfExec_dir;
//	protected String	s_asurfExec_exe;
	/** The URL of the  Surfaces webserver. */
//	protected	String		s_asurfURL = "http://localhost/scripts/lsmp/asurf/asurfCV3.exe";

	/** Class to load the geometries **/
	protected	PgJvxLoader	m_PgJvxLoader;


	public AbstractCGIClient(GeomStore store, String projName) {
		super(store, projName);
		m_PgJvxLoader = new PgJvxLoader();
	}

	public abstract String makeCGIstring();

	 /**
	  * Constructs the string to send to CGI server.
	  * Should be overwritten by subclasses to create the correct string.
	  */

//	abstract public String makeCGIstring();
	
	@Override
    public void init2() {
		if(PRINT_DEBUG) System.out.println("PjLC init2");
		//doFitDisplay = getParameter("AutoFit").equals("true");

		if(false && getParameter("UseDefaultModel").equals("true") 
			&& m_defModelName != null)
		{
			String fileName = getFullPath(m_defModelName);
			try
			{
				/** Data read in by the server **/
				
				PgJvxSrc m_PgJvxSrc[] = PgJvxLoader.read(fileName);
				processJvxSrc(m_PgJvxSrc,"OK",getBaseName());
			}
			catch(Exception e)
			{
				PsDebug.error("Error loading default model ("+fileName+")");
				System.out.println(e.getMessage());
				if(PRINT_DEBUG) e.printStackTrace();
			}
		}

		if(PRINT_DEBUG) System.out.println("PjLC init2 done");
	}

	/** Set The filename for the default surface displayed. 
	This must live in the same directory as the class files
	the full pathname will be reconstructed from this and codeBase.
	**/
	
	public void setDefaultFile(String filename)
	{
		m_defModelName = filename;
	}
	
	/** Gets The filename for the default surface displayed. **/
	
	public String getDefaultFile()
	{
		return m_defModelName;
	}

	/**
	 * Sets the name of the algebraic surfaces web server. 
	 * @param progname name of the server.
	 * @param serverDir base of the URL for the server.
	 * @param serverExtension extension for the server.
	 * @param execDir base of the URL for the executable.
	 * @param execExtension extension for the executable.
	 * If execDir is not null then there the program will be run
	 * by running (exec) a local program rather than by
	 * using a server on the internet.
	 * 
	 * The name of server is serverDir + progname + serverExtension.
	 * The name of executable is execDir + progname + execExtension.
	 */

	public void setCGIServer(String progname,int type,String serverDir,String serverExtension)
	{
		serverType = type;
		
		if(serverDir.endsWith("/") || serverDir.endsWith("\\"))
			server = serverDir + progname + serverExtension;
		else
			server = serverDir +"/"+ progname + serverExtension;
		
		if(PRINT_DEBUG) PsDebug.message("setCGIServer " + serverDir + " exec " + serverExtension );
	}


	 /**
	  * Connects to the URL of the surface server and gets the geometry.
	  */

		/**
		 *	Run the server communication in a thread.
		 */

	public boolean goCGI()
	{
		goCgiThread th = new goCgiThread(this.getBaseName());
		th.start();
		return true;
	}

	class goCgiThread extends Thread
	{
		String executable;
		String cgiReq;
		String localBaseName;
		PgJvxSrc m_PgJvxSrc[] = null;
		LmsPointSetMaterial point_mat = null;
		LmsPolygonSetMaterial line_mat = null;
		LmsElementSetMaterial face_mat = null;

		boolean successful=false;

		/** Create the thread. Makes local copies of most mutable variables at creation time. */
		
		goCgiThread(String base)
		{
			cgiReq = makeCGIstring();
			if(cgiReq==null) return;
			executable = new String(server);
			localBaseName = new String(getBaseName());
			//if(m_geom != null) face_mat = new LmsElementSetMaterial(m_geom);
			//if(m_point_geom != null) point_mat = new LmsPointSetMaterial(m_point_geom);
			//if(m_line_geom != null) line_mat = new LmsPolygonSetMaterial(m_line_geom);
		}

		/** run the thread. Creates a new process, writes data to it and reads the data from it.
			If successful call processJvxSrc to load into JavaView */

		@Override
        public void run()
		{
			if(PRINT_TIME)	timemessage("cgi_run:");
			
			URL myUrl = null;
			URLConnection connection = null;
			PrintWriter writer = null;
			BufferedReader reader = null;
		//      String inputLine;
		//	PrintWriter tmpout = null;
		
			// Create the URL of the Asurf server
		
			if(cgiReq == null) return;
			try
			{
				myUrl = new URL(executable);
			}
			catch( MalformedURLException e)
			{
				showStatus("Bad URL for server");
			        PsDebug.error("Bad URL for server "+executable);
		 		successful = false; return;
			}
		
			// Create the connection object
		
			try
			{
				connection = myUrl.openConnection();
			}
			catch(IOException e)
			{
				showStatus("Error opening connection to server");
				PsDebug.error("Error with openConnection");
				successful = false; return;
			}
		
			// Ensures that the we can write the server, i.e. do a POST request
		
			connection.setDoOutput(true);
		
			// and establish the stream 

			if(PRINT_TIME)	timemessage("cgi_run: sending data");
		
			showStatus("Sending data to server");
			try
			{
				writer = new PrintWriter(connection.getOutputStream());
			}
			catch(IOException e)
			{
				showStatus("Error writing data to server");
				PsDebug.error("Error opening writer");
				successful = false; return;
			}
		
			// write the request to the server
		
			writer.print(cgiReq);
			writer.close();

			if(PRINT_TIME)	timemessage("cgi_run: Calculating surface");

			showStatus("Calculating surface");
		
			// Now do the input
			String inputLine;
			try
			{
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
				/* The first line of the output contains any error messages genetated either
					ERROR ....
					WARNING ....
					OK ....
				   in the first case the geometry is bad, in the second case its incomplete
				   and in the third case its OK */
		
				inputLine = reader.readLine();
				if(inputLine == null)
				{
					showStatus("ERROR: no data returned from server");
					successful = false; return;
				}
				
				if(inputLine.startsWith("ERROR"))
				{
					showStatus(inputLine);
		//			PsDebug.message(inputLine);
					reader.close();
					successful = false; return;
				}
				else
				{
					showStatus(inputLine + " Loading data");
		//			PsDebug.message(inputLine + " Loading data");
					if(PRINT_TIME)	timemessage("cgi_run: "+ inputLine + " Loading data");
				}
		
		//		For debugging can write to a temp file
				if(DEBUG_ECHO_INPUT)
				{
					StringWriter sWrite = new StringWriter();
					PrintWriter pWrite = new PrintWriter(sWrite);
					String inputLine2;
		              		while ((inputLine2 = reader.readLine()) != null)
					{
						System.out.println(inputLine2);
			             		pWrite.println(inputLine2);
					}
					String buf = sWrite.toString();
					StringReader sRead = new StringReader(buf);
					BufferedReader bReader= new BufferedReader(sRead);
					m_PgJvxSrc = m_PgJvxLoader.read(bReader);
					sWrite.close();
					pWrite.close();
				}
				else
				{
					m_PgJvxSrc = m_PgJvxLoader.read(reader);
				}
		                reader.close();
				if(PRINT_TIME)	timemessage("cgi_run: finished reading data");
			}
			catch(IOException e)
			{
				showStatus("Error reading data from server");
				PsDebug.error("Error reading data from server: "+e.getMessage());
				successful = false; return;
			}
			
			String message = null;
			if(inputLine.startsWith("WARNING"))
				message = inputLine + " Surface partially loaded";
			else
				message = "Surface sucessfully loaded";

			if(PRINT_TIME)	timemessage("cgi_run: process src");
			successful = processJvxSrc(m_PgJvxSrc,message,localBaseName,face_mat,line_mat,point_mat);
			if(PRINT_TIME)	timemessage("cgi_run: finished!");
			return;
		} // end of run
	} // end of goCgiThread

	    /**
	     * Starts a local executable and get the geometry produced.
	     */

	public boolean goExec()
	{
		goExecThread th = new goExecThread(getBaseName());
		th.start();
		return true;
	}

		/**
		 *	Run the server communication in a thread.
		 */

	class goExecThread extends Thread
	{
		String executable;
		String envArray[] = new String[2];
		String cgiReq;
		String localBaseName;
		PgJvxSrc m_PgJvxSrc[] = null;
		LmsPointSetMaterial point_mat = null;
		LmsPolygonSetMaterial line_mat = null;
		LmsElementSetMaterial face_mat = null;

		boolean successful=false;

		/** Create the thread. Makes local copies of most mutable variables at creation time. */
		
		goExecThread(String base)
		{
			cgiReq = makeCGIstring();
			if(cgiReq==null) return;
			executable = new String(server);
			envArray[0]  = "REQUEST_METHOD=POST";
			envArray[1] = "CONTENT_LENGTH=" + cgiReq.length();
			localBaseName = new String(getBaseName());
			//if(m_geom != null) face_mat = new LmsElementSetMaterial(m_geom);
			//if(m_point_geom != null) point_mat = new LmsPointSetMaterial(m_point_geom);
			//if(m_line_geom != null) line_mat = new LmsPolygonSetMaterial(m_line_geom);
		}

		/** run the thread. Creates a new process, writes data to it and reads the data from it.
			If successful call processJvxSrc to load into JavaView */

		@Override
        public void run()
		{
			PrintWriter writer = null;
			BufferedReader reader = null;
	        	String inputLine;
//			PrintWriter tmpout = null;

			if(cgiReq == null) return;
			if(PRINT_TIME)	timemessage("cgi_run:");
			Runtime myRuntime = Runtime.getRuntime();
			Process myProcess = null;
//			String wd = getParameter("WorkingDir");
//			System.out.println("executable "+executable);
//			System.out.println("WorkingDir "+wd);
	/*
		File f;
			try
			{
				f = new File(getParameter("WorkingDir")); 
			}
			catch (Exception e) {
				PsDebug.error("Error with file");
				e.printStackTrace();
				successful = false; return;
			}

			if(f==null) { System.out.println("Null file");}  
			else
				{ System.out.println("file: "+f.toString());} 
	*/
//				String codeBase = PsConfig.getCodeBase();
//				System.out.println("codebase "+codeBase);
//				if(codeBase.startsWith("file:"))
//					codeBase = codeBase.substring(6);
//				System.out.println("codebase "+codeBase);
//				System.out.println("LSMP executable "+s_asurfExec_exe+" file "+s_asurfExec_dir);
//				myProcess = myRuntime.exec(executable,envArray,new File(codeBase+s_asurfExec_dir));
				//System.out.println("Sucessful process");

			try
			{
				myProcess = myRuntime.exec(executable,envArray);
			}
			catch (Exception e) {
				PsDebug.error("Error while trying to run C program");
				e.printStackTrace();
				successful = false; return;
			}
		
			if(PRINT_TIME)	timemessage("cgi_run: sending data");
			showStatus("Sending data to server");
//			PsDebug.message("Sending data to server");

			writer = new PrintWriter(myProcess.getOutputStream());
			writer.print(cgiReq);
			writer.close();

			if(PRINT_TIME)	timemessage("cgi_run: calculating surface");
			showStatus("Calculating surface");
//			PsDebug.message("Calculating surface");

//			int count = 0;
			try
			{
				reader = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));

	/*
				tmpout = new PrintWriter(new FileWriter(tf_tmpFile.getText()));
	        	        while ((inputLine = reader.readLine()) != null)
				{
					if(++count > 2)
						tmpout.println(inputLine);
				}
	*/

				String firstLine = reader.readLine();
				if(firstLine == null)
				{
					showStatus("ERROR: no data returned from server");
					successful = false; return;
				}
				if(PRINT_TIME)	timemessage("cgi_run: first line"+firstLine);

	        		String secondLine = reader.readLine();
				if(secondLine == null)
				{
					System.out.println("First line "+firstLine);
					showStatus("ERROR: 2nd line of data not returned from server");
					successful = false; return;
				}
				if(PRINT_TIME)	timemessage("cgi_run: 2nd line"+secondLine);

				inputLine = reader.readLine();
				if(inputLine == null)
				{
					System.out.println("First line "+firstLine);
					System.out.println("Second line "+secondLine);
					showStatus("ERROR: 3rd line of data not returned from server");
					successful = false; return;
				}
				if(PRINT_TIME)	timemessage("cgi_run: first line"+inputLine);

				if(inputLine.startsWith("ERROR"))
				{
					showStatus(inputLine);
					PsDebug.message(inputLine);
					reader.close();
					successful = false; return;
				}
				else
				{
					showStatus(inputLine + " Loading data");
//					PsDebug.message(inputLine + " Loading data");
				}
				if(PRINT_TIME)	timemessage("cgi_run: JvxLoad");

//				m_PgJvxSrc = m_PgJvxLoader.read(tf_tmpFile.getText());
				m_PgJvxSrc = m_PgJvxLoader.read(reader);
	                	reader.close();
//				tmpout.close();
			}
			catch(IOException e)
			{
				PsDebug.error("Error reading data from server: "+e.getMessage());
//				e.printStackTrace();
				successful = false; return;
			}

			String message = null;
			if(inputLine.startsWith("WARNING"))
				message = inputLine + " Surface partially loaded";
			else
				message = "Surface sucessfully loaded";

			if(PRINT_TIME)	timemessage("cgi_run: processJvxSrc");
			successful = processJvxSrc(m_PgJvxSrc,message,localBaseName,face_mat,line_mat,point_mat);
			if(PRINT_TIME)	timemessage("cgi_run: done");
		} // end of run

	} // end of goExecThread

	public boolean processJvxSrc(PgJvxSrc m_PgJvxSrc[],
		String message,
		String baseName)
	{
		LmsPointSetMaterial point_mat = null;
		LmsPolygonSetMaterial line_mat = null;
		LmsElementSetMaterial face_mat = null;

		return processJvxSrc(m_PgJvxSrc,message,baseName,face_mat,line_mat,point_mat);
	}

	/**
	 *	Load all the geometries read from server into the project.
	 *	This may be run by more than one thread at a time and some of the
	 *	class variables my be changed by javaview.
	 *
	 *	@param m_PgJvxSrc The geometry data read in from program.
	 *	@param message A message to display on completion.
	 *	@param baseName The name of created geometry, may have a unique identifier added.
	 *	@param face_mat Material properties to apply to faces.
	 *	@param line_mat Material properties to apply to lineSets.
	 *	@param point_mat Material properties to apply to pointSets.
	 */

	public boolean processJvxSrc(
		PgJvxSrc m_PgJvxSrc[],
		String message,
		String baseName,
		LmsElementSetMaterial face_mat,
		LmsPolygonSetMaterial line_mat,
		LmsPointSetMaterial point_mat)
	{
		boolean has_face = false, has_lines = false, has_points = false;
	  	         
		if(m_PgJvxSrc == null)
		{
			showStatus("Empty Geometry");
		//	PsDebug.message("Empty Geometry");
			return false;
		}

	        for(int i=0;i<m_PgJvxSrc.length;++i)
		{
			switch(m_PgJvxSrc[i].getType())
			{
			case PvGeometryIf.GEOM_ELEMENT_SET:
				if(has_face)
				{
					PsDebug.error("Two Element Sets in input");
				}
				else
				{
					if(cbCreateNew.getState())
						m_geom = new PgElementSet(3);
					m_geom.setJvx(m_PgJvxSrc[i]);
					m_geom.makeNeighbour();
					has_face = true;
				}
				break;

			case PvGeometryIf.GEOM_POLYGON_SET:
				if(has_lines)
				{
					PsDebug.error("Two Line Sets in input");
				}
				else
				{
					if(cbCreateNew.getState())
						m_line_geom = new PgPolygonSet(3);
					m_line_geom.setJvx(m_PgJvxSrc[i]);
					has_lines = true;
				}
				break;
			case PvGeometryIf.GEOM_POINT_SET:
				if(has_points)
				{
					PsDebug.error("Two Point Sets in input");
				}
				else
				{
					if(cbCreateNew.getState())
						m_point_geom = new PgPointSet(3);
					m_point_geom.setJvx(m_PgJvxSrc[i]);
					has_points = true;
				}
				break;
			default:
				PsDebug.error("Unknown geometry type in input: " + m_PgJvxSrc[i].getType() );
				break;
			}
		}
		
		/* now work out which geometries are in display and add/remove as appropriate */

		boolean showing_faces = false, showing_lines = false, showing_points = false;

		for(int i = 0;i<getNumGeometries();++i)
		{
			if(getGeometry(i) == m_geom) showing_faces = true;
			if(getGeometry(i) == m_line_geom) showing_lines = true;
			if(getGeometry(i) == m_point_geom) showing_points = true;
		}

		if(cbCreateNew.getState()) ++uniqueNum;
		
		if(has_face)
		{	
			if( cbKeepMat.getState() && face_mat != null)
				face_mat.apply(m_geom);
			if( cbColour.getState() )
			{
				if(m_geom.hasVertexColors())
					m_geom.makeElementFromVertexColors();
				else
					m_geom.makeElementColorsFromXYZ();
				m_geom.showElementColors(true);
                System.out.println("Individual ele colour ");
			}
			else {
				Color c = Color.getColor(this.chColours.getSelectedItem());
				m_geom.setGlobalElementColor(c);
                m_geom.showElementColors(false);
                System.out.println("Global ele colour "+m_geom.getGlobalElementColor());
			}
			if(uniqueNum != 0)
				m_geom.setName(baseName + "{"+uniqueNum+"}");
			else
				m_geom.setName(baseName);
	       		m_geom.update(m_geom);
//	System.out.println("Name "+m_geom.getName());
			if(!showing_faces) addGeometry(m_geom);
		}
		else
			if(showing_faces) removeGeometry(m_geom);

		if(has_lines)
		{	
			if(has_face)
			{
				if(uniqueNum != 0)
					m_line_geom.setName(baseName + " lines" + "{"+uniqueNum+"}");
				else
					m_line_geom.setName(baseName + " lines");
			}
			else
			{
				if(uniqueNum != 0)
					m_line_geom.setName(baseName + "{"+uniqueNum+"}");
				else
					m_line_geom.setName(baseName);
			}
			if( cbKeepMat.getState() && line_mat != null)
				line_mat.apply(m_line_geom);
	       		m_line_geom.update(m_line_geom);
			if(!showing_lines) addGeometry(m_line_geom);
		}
		else
			if(showing_lines) removeGeometry(m_line_geom);


		if(has_points)
		{	
			if(uniqueNum != 0)
				m_point_geom.setName(baseName + " points" + "{"+uniqueNum+"}");
			else
				m_point_geom.setName(baseName + " points");
			if( cbKeepMat.getState() && point_mat != null)
				point_mat.apply(m_point_geom);
	       		m_point_geom.update(m_point_geom);
			if(!showing_points) addGeometry(m_point_geom);
		}
		else
			if(showing_points) removeGeometry(m_point_geom);

		/* TODO
		PgGeometryIf dispgeoms[] = null;
		if(myViewer != null)
			dispgeoms = myViewer.getDisplay().getGeometries();

		if(has_face)
		{
			boolean flag = false;
			selectGeometry(m_geom);
			if(dispgeoms != null)
				for(int i=0;i<dispgeoms.length;++i)
				{
					if(dispgeoms[i] == m_geom) { flag = true; break; }
				}
			if(!flag && myViewer != null) myViewer.getDisplay().addGeometry(m_geom);
		}
		else if(has_lines)
		{
			boolean flag = false;
			selectGeometry(m_line_geom);
			if(dispgeoms != null)
				for(int i=0;i<dispgeoms.length;++i)
				{
					if(dispgeoms[i] == m_line_geom) { flag = true; break; }
				}
			if(!flag && myViewer != null) myViewer.getDisplay().addGeometry(m_line_geom);
		}
		else if(has_points)
		{
			boolean flag = false;
			selectGeometry(m_point_geom);
			if(dispgeoms != null)
				for(int i=0;i<dispgeoms.length;++i)
				{
					if(dispgeoms[i] == m_point_geom) { flag = true; break; }
				}
			if(!flag && myViewer != null) myViewer.getDisplay().addGeometry(m_point_geom);
		}

		if( doFitDisplay() )
			fitDisplays();
			*/
		showStatus(message);

		return true;

	} // end of processJvxSrc

	/** calculates the surface. */

	@Override
    public void calculate()
	{
		switch(serverType) {
		case NO_SERVER:
			equationChanged(taDef.getText());
			break;
		case CGI_SERVER:
	    	if( goCGI() )
	        {
	           if(PRINT_DEBUG) PsDebug.message("PjPsurfClient.update(): Successfully loaded geometry from server");
	        }
	        else
	        {
	           if(PRINT_DEBUG) PsDebug.message("PjPsurfClient.update(): failed to get data from server");
	        }
	    	break;
		case EXEC_SERVER:
	    	if( goExec() )
	        {
	    		if(PRINT_DEBUG) PsDebug.message("PjPsurfClient.update(): Successfully loaded geometry from executable");
	        }
	        else
	        {
	        	if(PRINT_DEBUG) PsDebug.message("PjPsurfClient.update(): failed to get data from executable");
	        }
	    	break;
		}
	}

}
