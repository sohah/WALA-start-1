/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.examples.drivers;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.strings.StringStuff;
import com.ibm.wala.core.viz.PDFViewUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.viz.DotUtil;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * This simple example WALA application builds a TypeHierarchy and fires off
 * ghostview to viz a DOT representation.
 * 
 * @author sfink
 */
public class PdfCFG {
  // This example takes one command-line argument, so args[1] should be the "-classpath" parameter
  final static int CLASSPATH_INDEX = 1;
  final static int METHOD_SIGNATURE_INDEX=2;


  public static void main(String[] args) throws IOException, ClassHierarchyException {
    Properties p = CommandLine.parse(args);
    String entryClass = p.getProperty("entryClass");
    String entryMethod = p.getProperty("entryMethod");
    String classpath = p.getProperty("classpath");
    run(entryClass, entryMethod, classpath);
  }


  public static void run(String entryClass, String entryMethod, String classpath)
      throws IOException, ClassHierarchyException {
    if (entryClass == null || entryMethod == null) {
      throw new IllegalArgumentException(
          "specify both an entryClass and an entryMethod for the analysis");
    }
    File exclusionFile = new File(PdfCFG.class.getResource("/WalaExclusions.txt").getFile());
    String classpathResource = PdfCFG.class.getResource("/aws-serverless-lambda-1.0-SNAPSHOT.jar").getFile().toString();
    AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(classpathResource,
                                                                                   exclusionFile);
//    addDefaultExclusions(scope);
    ClassHierarchy cha = ClassHierarchyFactory.make(scope);
    AnalysisOptions options = new AnalysisOptions();
//    options.getSSAOptions().setPiNodePolicy(SSAOptions.getAllBuiltInPiNodes());
    AnalysisCacheImpl cache = new AnalysisCacheImpl(options.getSSAOptions());
    MethodReference mr = StringStuff.makeMethodReference(entryClass + "." + entryMethod);
    IMethod m = cha.resolveMethod(mr);
    IR ir = cache.getIR(m, Everywhere.EVERYWHERE);
    SSACFG cfg = ir.getControlFlowGraph();
    draw(cha, ir, cfg);
  }



  public static Process draw(ClassHierarchy cha, IR ir, SSACFG cfg) throws IOException {
    try {

//      String dotFile = File.createTempFile("cfg", ".dt").getAbsolutePath();
//      String pdfFile = File.createTempFile("cfg", ".pdf").getAbsolutePath();
      String dotFile1 = new File("/media/soha/01D8C30C020FE690/git/WALA-start-1/cfg.dt").getAbsolutePath();
      String pdfFile1 = new File("/media/soha/01D8C30C020FE690/git/WALA-start-1/cfg.pdf").getAbsolutePath();
      String dotExe = "dot";
      String gvExe = "open";
      DotUtil.dotify(cfg, null, dotFile1, pdfFile1, dotExe);
//      return PDFViewUtil.launchPDFView(pdfFile1, gvExe);

      return PDFViewUtil.ghostviewIR(cha, ir,  pdfFile1, dotFile1, dotExe, gvExe);


    } catch (WalaException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  
  /**
   * Validate that the command-line arguments obey the expected usage.
   * 
   * Usage: args[0] : "-classpath" args[1] : String, a ";"-delimited class path
   * 
   * @throws UnsupportedOperationException if command-line is malformed.
   */
  public static void validateCommandLine(String[] args) {
    if (args.length < 2) {
      throw new UnsupportedOperationException("must have at least 2 command-line arguments");
    }
    if (!args[0].equals("-classpath")) {
      throw new UnsupportedOperationException("invalid command-line, args[0] should be -classpath, but is " + args[0]);
    }
  }
}
