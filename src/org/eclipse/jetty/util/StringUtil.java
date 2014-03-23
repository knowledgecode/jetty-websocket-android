//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/** Fast String Utilities.
 *
 * These string utilities provide both conveniance methods and
 * performance improvements over most standard library versions. The
 * main aim of the optimizations is to avoid object creation unless
 * absolutely required.
 *
 * 
 */
public class StringUtil
{
    private static final Logger LOG = Log.getLogger(StringUtil.class);

    public static final String ALL_INTERFACES="0.0.0.0";
    public static final String CRLF="\015\012";
    public static final String __LINE_SEPARATOR=
        System.getProperty("line.separator","\n");

    public static final String __ISO_8859_1="ISO-8859-1";
    public final static String __UTF8="UTF-8";
    public final static String __UTF8Alt="UTF8";
    public final static String __UTF16="UTF-16";

    public final static Charset __UTF8_CHARSET;
    public final static Charset __ISO_8859_1_CHARSET;

    static
    {
        __UTF8_CHARSET=Charset.forName(__UTF8);
        __ISO_8859_1_CHARSET=Charset.forName(__ISO_8859_1);
    }

    /* ------------------------------------------------------------ */
    /**
     * replace substrings within string.
     */
    public static String replace(String s, String sub, String with)
    {
        int c=0;
        int i=s.indexOf(sub,c);
        if (i == -1)
            return s;

        StringBuilder buf = new StringBuilder(s.length()+with.length());

        do
        {
            buf.append(s.substring(c,i));
            buf.append(with);
            c=i+sub.length();
        } while ((i=s.indexOf(sub,c))!=-1);

        if (c<s.length())
            buf.append(s.substring(c,s.length()));

        return buf.toString();
    }

    /* ------------------------------------------------------------ */
    /**
     * append hex digit
     * 
     */
    public static void append(StringBuilder buf,byte b,int base)
    {
        int bi=0xff&b;
        int c='0'+(bi/base)%base;
        if (c>'9')
            c= 'a'+(c-'0'-10);
        buf.append((char)c);
        c='0'+bi%base;
        if (c>'9')
            c= 'a'+(c-'0'-10);
        buf.append((char)c);
    }

    /* ------------------------------------------------------------ */
    public static void append2digits(StringBuilder buf,int i)
    {
        if (i<100)
        {
            buf.append((char)(i/10+'0'));
            buf.append((char)(i%10+'0'));
        }
    }

    /* ------------------------------------------------------------ */
    public static String toString(byte[] b,int offset,int length,String charset)
    {
        try
        {
            return new String(b,offset,length,charset);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static byte[] getBytes(String s)
    {
        try
        {
            return s.getBytes(__ISO_8859_1);
        }
        catch(Exception e)
        {
            LOG.warn(e);
            return s.getBytes();
        }
    }
}
