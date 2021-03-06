/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.acme.cdiwab.impl;

import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acme.stockquoteservice.api.StockQuoteService;
import org.glassfish.osgicdi.OSGiService;

/**
 * A simple Servlet to use the Stock Quote service
 * 
 * @author Sivakumar Thyagarajan
 */
@WebServlet(urlPatterns = "/list")
public class StockQuoteServlet extends HttpServlet {
    //Inject the OSGi service by specifying the OSGiService qualifier
    @Inject
    @OSGiService(/* dynamically bound */ dynamic = true,
            /* wait for 1 min */ waitTimeout=30*1000)
    StockQuoteService sqs;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<HTML> <HEAD> <TITLE> Latest Stock Quotes </TITLE> </HEAD> ");
        out.println("<BODY BGCOLOR=white>");
        try {
            if (sqs != null) {
                //get the list of symbols and print their current quotes
                try {
                    out.println("<table border=\"1\">");
                    for(String sym: sqs.getSymbols()){
                        out.println("<tr>");
                        out.println("<td>" + sym + "</td>");
                        out.println("<td>" + sqs.getQuote(sym) + "</td>");
                        out.println("</tr>");
                    }
                    out.println("</table>");
                } catch (Exception e) {
                    out.println("Service unavailable");
                    e.printStackTrace();
                }
            } else {
                out.println("Stock Symbol Service is not yet available");
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
        out.println("</BODY> </HTML> ");
    }
}
