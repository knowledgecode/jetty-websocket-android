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

/* ------------------------------------------------------------ */
/** Fast B64 Encoder/Decoder as described in RFC 1421.
 * <p>Does not insert or interpret whitespace as described in RFC
 * 1521. If you require this you must pre/post process your data.
 * <p> Note that in a web context the usual case is to not want
 * linebreaks or other white space in the encoded output.
 * 
 */
public class B64Code
{
    // ------------------------------------------------------------------
    static final char __pad='=';
    static final char[] __rfc1421alphabet=
            {
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
                'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
                'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
                'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
            };

    static final byte[] __rfc1421nibbles;

    static
    {
        __rfc1421nibbles=new byte[256];
        for (int i=0;i<256;i++)
            __rfc1421nibbles[i]=-1;
        for (byte b=0;b<64;b++)
            __rfc1421nibbles[(byte)__rfc1421alphabet[b]]=b;
        __rfc1421nibbles[(byte)__pad]=0;
    }

    // ------------------------------------------------------------------
    /**
     * Fast Base 64 encode as described in RFC 1421.
     * <p>Does not insert whitespace as described in RFC 1521.
     * <p> Avoids creating extra copies of the input/output.
     * @param b byte array to encode.
     * @return char array containing the encoded form of the input.
     */
    static public char[] encode(byte[] b)
    {
        if (b==null)
            return null;

        int bLen=b.length;
        int cLen=((bLen+2)/3)*4;
        char c[]=new char[cLen];
        int ci=0;
        int bi=0;
        byte b0, b1, b2;
        int stop=(bLen/3)*3;
        while (bi<stop)
        {
            b0=b[bi++];
            b1=b[bi++];
            b2=b[bi++];
            c[ci++]=__rfc1421alphabet[(b0>>>2)&0x3f];
            c[ci++]=__rfc1421alphabet[(b0<<4)&0x3f|(b1>>>4)&0x0f];
            c[ci++]=__rfc1421alphabet[(b1<<2)&0x3f|(b2>>>6)&0x03];
            c[ci++]=__rfc1421alphabet[b2&077];
        }

        if (bLen!=bi)
        {
            switch (bLen%3)
            {
                case 2:
                    b0=b[bi++];
                    b1=b[bi++];
                    c[ci++]=__rfc1421alphabet[(b0>>>2)&0x3f];
                    c[ci++]=__rfc1421alphabet[(b0<<4)&0x3f|(b1>>>4)&0x0f];
                    c[ci++]=__rfc1421alphabet[(b1<<2)&0x3f];
                    c[ci++]=__pad;
                    break;

                case 1:
                    b0=b[bi++];
                    c[ci++]=__rfc1421alphabet[(b0>>>2)&0x3f];
                    c[ci++]=__rfc1421alphabet[(b0<<4)&0x3f];
                    c[ci++]=__pad;
                    c[ci++]=__pad;
                    break;

                default:
                    break;
            }
        }

        return c;
    }

    /* ------------------------------------------------------------ */
    public static void encode(long lvalue,Appendable buf) throws IOException
    {
        int value=(int)(0xFFFFFFFC&(lvalue>>32));
        buf.append(__rfc1421alphabet[0x3f&((0xFC000000&value)>>26)]);
        buf.append(__rfc1421alphabet[0x3f&((0x03F00000&value)>>20)]);
        buf.append(__rfc1421alphabet[0x3f&((0x000FC000&value)>>14)]);
        buf.append(__rfc1421alphabet[0x3f&((0x00003F00&value)>>8)]);
        buf.append(__rfc1421alphabet[0x3f&((0x000000FC&value)>>2)]);

        buf.append(__rfc1421alphabet[0x3f&((0x00000003&value)<<4) + (0xf&(int)(lvalue>>28))]);

        value=0x0FFFFFFF&(int)lvalue;
        buf.append(__rfc1421alphabet[0x3f&((0x0FC00000&value)>>22)]);
        buf.append(__rfc1421alphabet[0x3f&((0x003F0000&value)>>16)]);
        buf.append(__rfc1421alphabet[0x3f&((0x0000FC00&value)>>10)]);
        buf.append(__rfc1421alphabet[0x3f&((0x000003F0&value)>>4)]);
        buf.append(__rfc1421alphabet[0x3f&((0x0000000F&value)<<2)]);
    }
}
