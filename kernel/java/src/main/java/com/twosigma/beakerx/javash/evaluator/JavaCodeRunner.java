/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.twosigma.beakerx.javash.evaluator;

import com.twosigma.beakerx.evaluator.InternalVariable;
import com.twosigma.beakerx.jvm.object.SimpleEvaluationObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class JavaCodeRunner implements Runnable {

  protected final SimpleEvaluationObject theOutput;
  protected final Method theMth;
  protected final boolean retObject;
  protected final ClassLoader loader;

  public JavaCodeRunner(Method mth, SimpleEvaluationObject out, boolean ro, ClassLoader ld) {
    theMth = mth;
    theOutput = out;
    retObject = ro;
    loader = ld;
  }

  @Override
  public void run() {
    ClassLoader oldld = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(loader);
    theOutput.setOutputHandler();
    InternalVariable.setValue(theOutput);
    try {
      InternalVariable.setValue(theOutput);
      Object o = theMth.invoke(null, (Object[]) null);
      if (retObject) {
        theOutput.finished(o);
      } else {
        theOutput.finished(null);
      }
    } catch (Throwable e) {
      if (e instanceof InvocationTargetException)
        e = ((InvocationTargetException) e).getTargetException();
      if ((e instanceof InterruptedException) || (e instanceof ThreadDeath)) {
        theOutput.error("... cancelled!");
      } else {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        theOutput.error(sw.toString());
      }
    } finally {
      if (theOutput != null) {
        theOutput.executeCodeCallback();
      }
    }
    theOutput.clrOutputHandler();
    Thread.currentThread().setContextClassLoader(oldld);
  }

}