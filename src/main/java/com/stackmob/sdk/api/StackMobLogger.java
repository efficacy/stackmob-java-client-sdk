/**
 * Copyright 2012 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stackmob.sdk.api;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackMobLogger {
    
    private boolean enableLogging = false;
    
    public void setLogging(boolean logging) {
        enableLogging = logging;
    }
    

    public void logDebug(String format, Object... args) {
        if(enableLogging) System.out.println(String.format(format, args));
    }

    public void logInfo(String format, Object... args) {
        if(enableLogging) System.out.println(String.format(format, args));
    }

    public void logWarning(String format, Object... args) {
        if(enableLogging) System.out.println(String.format(format, args));
    }

    public void logError(String format, Object... args) {
        if(enableLogging) System.err.println(String.format(format, args));
    }
    
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
