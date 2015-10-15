/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): AppIQ, Inc.____________________________
 *
 * 修 改 人:  l90003110 李松涛
 * 修改时间:  2009-02-04
 * 修改原因:  单一日志文件太大
 * 修改内容:  动态生成多个日志文件,命名规则:前缀_YYYYMMDD_N.log
 *           参见:private static void updateLogFile()
*/

package javax.wbem.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * The Debug class provides the ability to write debug trace statements
 * to an output device; either stdout, stderr, or a file.  Tracing is
 * controlled by two system properties:
 *.p
 * wbem.debug.level  - Sets the level of detail of tracing statements
 * wbem.debug.device - Sets the output device: stdout, sdterr, file
 *.p
 * The trace level is a combination of detail level and optional
 * information to be included on each statement.  The level is
 * an integer value from zero to three, with zero indicating that
 * no tracing should be done, one indicating errors should be
 * reported, two indicating errors and major events should be
 * reported, and three indicating all trace statements should
 * be reported (the detailed level).  Optional information is
 * controlled by adding one or more modifiers to the level number,
 * including "t" to include a time stamp, "m" to include the class
 * and method name writing the trace statement, and "p" to include
 * the thread identifier.  Thus, a value for wbem.debug.level
 * might appear as
 *.p
 * wbem.debug.level=3tmp
 *.p
 * If the debug device is set to "file", a default trace filename
 * consisting of the name "wbem_client_mmdd_hhmm" is used for client
 * side tracing, and the name "wbem_server_mmdd_hhmm" is used for
 * server side tracing.  The mmdd_hhmm is the current time the trace
 * file is opened.  The default directory for the trace file is
 * /var/tmp.  The debug device may be set to a fully qualified
 * file path name, if desired.  If the client application cannot
 * write to the log file, tracing will be turned off.
 *
 * @version 1.3  04/13/01
 * @author	Sun Microsystems, Inc.
 */
public final class Debug {

    // =====================================================================
    //
    // Static define constant to control compilation of trace methods
    //
    // =====================================================================

    // If this flag is set to true, the trace method implementation code
    // will be active and tracing can be controlled through the runtime
    // wbem.debug.level system property.  If this flag is set to false,
    // the compiler should remove the method implementation, leaving an
    // empty method.  This should allow JIT compilers to inline these
    // methods, resulting in the debugging trace method call being
    // removed from the tracing code!

    // XXXX - Typically set to true for development phases, reset to
    // XXXX - false for shipped code.

    private static final boolean ON = true;

    // =====================================================================
    //
    // Private define constants
    //
    // =====================================================================

    private final static String TRACE_DIR = "/var/tmp";
    private final static String TRACE_STDOUT_NAME = "stdout";
    private final static String TRACE_STDERR_NAME = "stderr";
    private final static int TRACE_OFF = 0;
    private final static int TRACE_STDOUT = 1;
    private final static int TRACE_STDERR = 2;
    private final static int TRACE_FILE = 3;
    private final static int TRACE_RETRY = 5;

    // =====================================================================
    //
    // Private attributes
    //
    // =====================================================================

    // Private static attributes
    private static boolean trace_init = false;
    private static int trace_level = TRACE_OFF;
    private static boolean trace_time = false;
    private static boolean trace_method = false;
    private static boolean trace_thread = false;
    private static int trace_out = TRACE_STDERR;
    private static FileWriter trace_fw = null;
    private static BufferedWriter trace_bw = null;
    private static PrintWriter trace_pw = null;
    private static String LOG_NAME = null; // 日志文件路径+前缀
    
    private static String LOG_DATE = null;// 日志日期
    
    private static int LOG_NUMBER = 1; // 日志编号
    private static int LOG_MAXBYTES; // 单个日志最大容量 单位:字节
    static
    {
        String logSize = System.getProperty("wbem.debug.size", "10");
        if (!logSize.equals("0"))
        {
            LOG_MAXBYTES = new Integer(logSize).intValue() * 1024 * 1024;
        }
        else
        {
            LOG_MAXBYTES = 10 * 1024 * 1024;
        }
        
        LOG_NAME = getLogDir();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        LOG_DATE = sdf.format(new Date());//当天
    }

    // =====================================================================
    //
    // Public static methods
    //
    // =====================================================================

    /**
     * The traceOpen method initializes the client or server
     * for debug tracing.  The level of tracing is specified as an integer
     * from zero (no tracing) to three (most detailed tracing) with
     * optional characters to indicate additional message prefix informatino.
     * The trace file name argument can specify output to standard out,
     * standard error, or a specific log trace file name.  The management
     * client and management server will each specify a different trace
     * file name.  The trace file will be written to the local system's
     * /var/log directory.
     *
     * @param	level	    The debug trace level: {0|1|2|3} + t, m, p
     * @param	filename    The debug trace log file name, stdout, or stderr
     */
    public static final void traceOpen(String level, String filename) {

	if (Debug.ON) {
	    openTrace(level, filename);
	}

    }

    /**
     * The isOn method returns true if debug tracing is configured
     * and the debug trace level is greater than zero (tracing is
     * enabled at some level).
     *
     * @return	True if some level of tracing is enabled
     */
    public static final boolean isOn() {

        if (Debug.ON) {
	    if (trace_level > 0) {
		return (true);
	    }
	}
	return (false);

    }

    /**
     * This debug trace message method writes the message to the trace
     * log device if we are tracing at level 1.
     *
     * @param	message The debug trace message
     */
    public static final void trace1(String message) {

	if (Debug.ON) {
	    if (trace_level > 0) {
		writeTrace(message);
	    }
	}

    }

    /**
     * This debug trace message method writes the message and an exception
     * stack trace to the log if we are tracing at level 1.
     *
     * @param	message	The debug trace message
     * @param	ex	The exception to trace back
     */
    public static final void trace1(String message, Throwable ex) {

	if (Debug.ON) {
	    if (trace_level > 0) {
		writeTrace(message);
		if (ex != null) {
		    writeStackTrace(ex);
		}
	    }
	}

    }

    /**
     * This debug trace message method writes the message to the trace
     * log device if we are tracing at level 2.
     *
     * @param	message The debug trace message
     */
    public static final void trace2(String message) {

	if (Debug.ON) {
	    if (trace_level > 1) {
		writeTrace(message);
	    }
	}

    }

    /**
     * This debug trace message method writes the message and an exception
     * stack trace to the log if we are tracing at level 2.
     *
     * @param	message	The debug trace message
     * @param	ex	The exception to trace back
     */
    public static final void trace2(String message, Throwable ex) {

	if (Debug.ON) {
	    if (trace_level > 1) {
		writeTrace(message);
		if (ex != null) {
		    writeStackTrace(ex);
		}
	    }
	}

    }

    /**
     * This debug trace message method writes the message to the trace
     * log device if we are tracing at level 3.
     *
     * @param	message The debug trace message
     */
    public static final void trace3(String message)
    {
        
        if (Debug.ON)
        {
            if (trace_level > 2)
            {
                writeTrace(message);
            }
        }
    }

    public static final void trace3(Document request)
    {
        
        if (Debug.ON)
        {
            if (trace_level > 2)
            {
                // Use a Transformer for output
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer;
                try
                {
                    transformer = tFactory.newTransformer();
                    DocumentType docType = request.getDoctype();
                    if (docType != null)
                    {
                        String systemId = docType.getSystemId();
                        if (systemId != null)
                        {
                            String systemValue = (new File(systemId)).getName();
                            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                                    systemValue);
                        }
                    }
                    DOMSource source = new DOMSource(request);
                    StreamResult result = null;
                    if (trace_out == TRACE_FILE)
                    {
                        updateLogFile();
                        result = new StreamResult(trace_pw);
                    }
                    else if (trace_out == TRACE_STDOUT)
                    {
                        result = new StreamResult(System.out);
                    }
                    else if (trace_out == TRACE_STDERR)
                    {
                        result = new StreamResult(System.err);
                    }
                    
                    if (null != result)
                    {
                        transformer.transform(source, result);
                    }
                }
                catch (TransformerConfigurationException e)
                {
                    //e.printStackTrace();
                }
                catch (TransformerException e)
                {
                    //e.printStackTrace();
                }
            }
        }
        
    }
    /**
     * This debug trace message method writes the message and an exception
     * stack trace to the log if we are tracing at level 3.
     *
     * @param	message	The debug trace message
     * @param	ex	The exception to trace back
     */
    public static final void trace3(String message, Throwable ex) {

	if (Debug.ON) {
	    if (trace_level > 2) {
		writeTrace(message);
		if (ex != null) {
		    writeStackTrace(ex);
		}
	    }
	}

    }

    // ********************************************************************
    //
    // Private methods
    //
    // *******************************************************************

    // Internal method to open the trace log file
    private static void openTrace(String level, String filename) {

	String trace_file = null;

	if (trace_init)
	    return;

	// Get the trace level and any optional flags
	trace_level = TRACE_OFF;
	trace_time = false;
	trace_method = false;
	trace_thread = false;
	if (level != null) {
	    Integer ix;
	    try {
		ix = new Integer(level.substring(0,1));
	    } catch (Exception ex) {
		ix = new Integer(0);
	    }
	    trace_level = ix.intValue();
	    if (level.indexOf('t') > 0) {
		trace_time = true;
	    }
	    if (level.indexOf('m') > 0) {
		trace_method = true;
	    }
	    if (level.indexOf('p') > 0) {
		trace_thread = true;
	    }
	}

	// If tracing turned off at runtime, just return.
	if (trace_level == 0) {
	    return;
	}

	// Set the output device for tracing.  Must be stdout, stderr,
	// or a file name.  If invalid, set tracing off silently!
	if ((filename != null) && (filename.trim().length() != 0)) {
	    if (filename.equals(TRACE_STDOUT_NAME))
		trace_out = TRACE_STDOUT;
	    else if (filename.equals(TRACE_STDERR_NAME))
		trace_out = TRACE_STDERR;
	    else {
		trace_out = TRACE_FILE;
		trace_file = filename.trim();
	    }
	} else {
	    // Invalid or null trace file name; default to stderr.
	    // <PJA> 16-Jan-2003
	    // set trace_out not trace_file!
	    trace_out = TRACE_STDERR;
	}

	// If tracing to a file, form the fully qualified path name to the
	// file.  Trace file suffix is .MMDD_HHMM from current time.
	// Trace file will be opened in the system temp directory.
	// If it already exists, add a numeric suffix until not found.
	//for fortify
	if (null == trace_file)
	{
		throw new NullPointerException();
	}
	if ((trace_out == TRACE_FILE) && (trace_level > 0)) {
	    if (trace_file.indexOf(File.separatorChar) < 0) {
                //trace_file = getLogDir() + File.separator + trace_file;
                LOG_NAME = LOG_NAME + File.separator + trace_file;
	    }
            updateLogFile();
        }
        // Indicate we have initialized tracing
        trace_init = true;
    }
    /*
     * 更新日志文件输出流
     * Add by l90003110 20090204
     */
    synchronized private static void updateLogFile()
    {
        File f = null;
        try
        {
            if ((null != trace_pw) && (null != trace_bw) && (null != trace_fw))
            {
                //Calendar rightNow = Calendar.getInstance();
                //int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                //int minute = rightNow.get(Calendar.MINUTE);
                //                if (!((25 <= minute) && (30 >= minute)))
                //                {
                //                    return;
                //                }
                trace_pw.flush();
            }
            else
            {
                f = new File(LOG_NAME + "_" + LOG_DATE + "_" + LOG_NUMBER
                        + ".log");
                trace_fw = new FileWriter(f, true);
                trace_bw = new BufferedWriter(trace_fw);
                trace_pw = new PrintWriter(trace_bw);//, true);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String trace_sufx = sdf.format(new Date());// 新日志日期
            if (!LOG_DATE.equals(trace_sufx)) // 第二天
            {
                LOG_DATE = trace_sufx;
                LOG_NUMBER = 1;
            }
            
            int logNumCurr = LOG_NUMBER; // 原编号
            while (true)
            {
                f = new File(LOG_NAME + "_" + LOG_DATE + "_" + LOG_NUMBER
                        + ".log");
                if (f.exists()) // 存在
                {
                    if (LOG_MAXBYTES < f.length())//fis.available())//超过最大限制 递增LOG_NUMBER
                    {
                        LOG_NUMBER = LOG_NUMBER + 1;
                        continue;
                    }
                    else
                    // 容量正常
                    {
                        if (logNumCurr == LOG_NUMBER) // 当前日志文件
                        {
                            return;
                        }
                        else // 更换了日志文件
                        {
                            break;
                        }
                    }
                }
                else
                // 不存在
                {
                    f.createNewFile();
                    break;
                }
            }
            
            if (null != f)
            {
                trace_pw.close();
                trace_bw.close();
                trace_fw.close();
                
                trace_fw = new FileWriter(f, true);
                trace_bw = new BufferedWriter(trace_fw);
                trace_pw = new PrintWriter(trace_bw);//, true);
            }
        }
        catch (Exception ex)
        {
            // Eat exceptions and turn off tracing if errors
            //trace_level = 0;
        }
    }

    // Internal method to write an exception stack trace to the log.

    private static void writeStackTrace(Throwable ex) {

	try {
	    if (trace_out == TRACE_FILE) {
                updateLogFile();
		ex.printStackTrace(trace_pw);
	    } else if (trace_out == TRACE_STDOUT) {
		ex.printStackTrace(System.out);
	    } else if (trace_out == TRACE_STDERR) {
		ex.printStackTrace(System.err);
	    }
	} catch (Exception x) {
	    // Eat exceptions
	}

    }

    // Return the trace log file directory

    private static String getLogDir() {

	// For now, this is fixed.  Need to make is smarter when
	// running client on a Wintel machine.
        //setting the path for saving the trace information
        String retPath = "";
        String path = System.getProperty("ISMPATH");
        if (null != path && 0 < path.trim().length())
        {
            retPath = path + "/logs/wbem/";
        }
        else
        {
            retPath = (TRACE_DIR);
        }
        
        File tmpFile = new File(retPath);
        if (!tmpFile.exists())
        {
            tmpFile.mkdirs();
        }
        return retPath;
    }
    // Return the class name and method name that called the trace method.

    private static String getClassMethod() {

	String line;
	String clm;

	clm = null;
	try {
	    InputStream is = getStackStream();
	    BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    br.readLine();		// Skip top five lines...
	    br.readLine();
	    br.readLine();
	    br.readLine();
	    br.readLine();
	    line = br.readLine();	// This should be caller from stack
	    clm = getCaller(line);	// Pull out class and method name
	    br.close();
	} catch (Exception ex) {
	    clm = "??:??";		// If any errors, don't know names
	}

	return (clm);
    }

    // Write trace message.  Ignore exceptions...

    private static synchronized void writeTrace(String msg) {

	String trace_msg = "";
	if (trace_time) {
	    Time tim = new Time(System.currentTimeMillis());
	    trace_msg = tim.toString() + " | ";
	}
	if (trace_thread) {
	    Thread th = Thread.currentThread();
	    trace_msg = trace_msg + th.getName() + " | ";
	}
	if (trace_method) {
	    trace_msg = trace_msg + getClassMethod() + " | ";
	}
	trace_msg = trace_msg + msg;
	try {
	    if (trace_out == TRACE_FILE) {
                updateLogFile();
                trace_pw.println();
		trace_pw.println(trace_msg);
	    } else if (trace_out == TRACE_STDOUT) {
                System.out.println();
		System.out.println(trace_msg);
	    } else if (trace_out == TRACE_STDERR) {
                System.err.println();
		System.err.println(trace_msg);
	    }
	} catch (Exception ex) {
	    // Eat exceptions
	}

    }

    // Get stack trace for determining calling class and method

    private static InputStream getStackStream() {

	ByteArrayInputStream is = null;
	ByteArrayOutputStream os = new ByteArrayOutputStream();

	try {
	    PrintWriter pw = new PrintWriter(os);
	    new Exception().printStackTrace(pw);
	    pw.close();
	    is = new ByteArrayInputStream(os.toByteArray());
	} catch (Exception ex) {
	    is = null;
	}

	return (is);

    }

    // Get class name and method name from stack trace line

    private static String getCaller(String line) {

	String str, mth, cls;
	int i;

	str = line;
	
	if(null == line)
	{
		throw new NullPointerException();
	}
	
	i = line.indexOf('(');
	if (i > 0)
	    str = line.substring(0, i);
	i = str.indexOf("at");
	if (i > 0)
	    str = str.substring(i+3);
	i = str.lastIndexOf('.');
	if (i > 0) {
	    mth = str.substring(i+1);
	    str = str.substring(0, i);
	    i = str.lastIndexOf('.');
	    if (i > 0)
		cls = str.substring(i+1);
	    else
		cls = str;
	    str = cls + ":" + mth;
	}

	return (str);
    }

}
