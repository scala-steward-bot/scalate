/*
  * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package org.fusesource.scalate.util

import _root_.org.fusesource.scalate.RenderContext
import collection.mutable.LinkedHashMap


object RenderHelper
{

  /**
   * Pads the text following newlines with the specified
   * indent amount so that the text is indented.
   */
  def indent( amount:String, text: String ): String = text.replaceAll("\n(.)", "\n"+amount+"$1" )

  def indentAmount( level:Int, kind:String ): String = {
    val rc = new StringBuilder
    var i=0;
    while(i < level) {
      rc.append(kind)
      i+=1;
    }
    rc.toString
  }

  /**
   * Converts newlines into the XML entity: &#x000A;
   * so that multiple lines encode to a sinle long HTML source line
   * but still render in browser as multiple lines.
   */
  def preserve( text: String ): String = text.replaceAll("\n", "&#x000A;");

  /**
   *  Escapes any XML special characters.
   */
  def sanitize( text: String ): String =
    text.foldLeft( new StringBuffer )( (acc, ch) => sanitize( ch, acc ) ).toString


  private def sanitize( ch: Char, buffer: StringBuffer ): StringBuffer = {
    if( ( ch >= 0x20 && ch <= 0x21 ) ||
        ( ch >= 0x23 && ch <= 0x25 ) ||
        ( ch >= 0x28 && ch <= 0x3B ) ||
        ( ch >= 0x3F && ch <= 0x7E ) ||
          ch == 0x3D || ch == '\'' ||
          ch == '\r' || ch == '\n') {
      buffer.append( ch )
    } else {
      buffer.append( ch match {
        case '"' => { "&quot;" }
        case '&' => { "&amp;" }
        case '<' => { "&lt;" }
        case '>' => { "&gt;" }
        case _   => {  "&#x" + ch.toInt.toHexString + ";" }
      })
    }
  }


  def attributes(context:RenderContext, entries: List[(Any,Any)]) {

    val (entries_class, tmp) = entries.partition{x=>{ x._1 match { case "class" => true; case _=> false} } }
    val (entries_id, entries_rest) = tmp.partition{x=>{ x._1 match { case "id" => true; case _=> false} } }
    var map = LinkedHashMap[Any,Any]( )

    if( !entries_id.isEmpty ) {
      map += "id" -> entries_id.last._2
    }

    if( !entries_class.isEmpty ) {
      val value = entries_class.map(x=>x._2).mkString(" ")
      map += "class"->value
    }

    entries_rest.foreach{ me => map += me._1 -> me._2 }

    if( !map.isEmpty ) {
      map.foreach {
        case (name,value) =>
        if( value!=null && (!value.isInstanceOf[Boolean] || value.asInstanceOf[Boolean])) {
          context << " "
          context << (context.value(name))
          context << ("=\"")
          context << (sanitize(context.value(value)))
          context << ("\"")
        }
      }
    }

  }

}
