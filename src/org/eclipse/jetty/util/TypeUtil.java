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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* ------------------------------------------------------------ */
/**
 * TYPE Utilities.
 * Provides various static utiltiy methods for manipulating types and their
 * string representations.
 *
 * @since Jetty 4.1
 */
public class TypeUtil
{
    /* ------------------------------------------------------------ */
    /** Array to List.
     * <p>
     * Works like {@link Arrays#asList(Object...)}, but handles null arrays.
     * @return a list backed by the array.
     */
    public static <T> List<T> asList(T[] a)
    {
        if (a==null)
            return Collections.emptyList();
        return Arrays.asList(a);
    }

    /* ------------------------------------------------------------ */
    /** Parse an int from a substring.
     * Negative numbers are not handled.
     * @param s String
     * @param offset Offset within string
     * @param length Length of integer or -1 for remainder of string
     * @param base base of the integer
     * @return the parsed integer
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static int parseInt(String s, int offset, int length, int base)
        throws NumberFormatException
    {
        int value=0;

        if (length<0)
            length=s.length()-offset;

        for (int i=0;i<length;i++)
        {
            char c=s.charAt(offset+i);

            int digit=convertHexDigit((int)c);
            if (digit<0 || digit>=base)
                throw new NumberFormatException(s.substring(offset,offset+length));
            value=value*base+digit;
        }
        return value;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param c An ASCII encoded character 0-9 a-f A-F
     * @return The byte value of the character 0-16.
     */
    public static int convertHexDigit( int c )
    {
        int d= ((c & 0x1f) + ((c >> 6) * 0x19) - 0x10);
        if (d<0 || d>15)
            throw new NumberFormatException("!hex "+c);
        return d;
    }

    /* ------------------------------------------------------------ */
    public static void toHex(byte b,Appendable buf)
    {
        try
        {
            int d=0xf&((0xF0&b)>>4);
            buf.append((char)((d>9?('A'-10):'0')+d));
            d=0xf&b;
            buf.append((char)((d>9?('A'-10):'0')+d));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------------------------------------------ */
    public static String toHexString(byte b)
    {
        return toHexString(new byte[]{b}, 0, 1);
    }

    /* ------------------------------------------------------------ */
    public static String toHexString(byte[] b,int offset,int length)
    {
        StringBuilder buf = new StringBuilder();
        for (int i=offset;i<offset+length;i++)
        {
            int bi=0xff&b[i];
            int c='0'+(bi/16)%16;
            if (c>'9')
                c= 'A'+(c-'0'-10);
            buf.append((char)c);
            c='0'+bi%16;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
        }
        return buf.toString();
    }
}
