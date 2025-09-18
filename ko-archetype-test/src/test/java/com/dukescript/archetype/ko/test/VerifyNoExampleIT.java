/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2015 Anton Epple <toni.epple@eppleton.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dukescript.archetype.ko.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 *
 * @author Anton Epple
 */
public class VerifyNoExampleIT extends VerifyArchetypeIT {
    @Override
    protected void assertHTMLContent(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (;;) {
            String l = br.readLine();
            if (l == null) {
                br.close();
                return;
            }
            if (l.contains("foreach:")) {
                fail("There should be no 'foreach:' in the file");
            }
        }
    }

    @Override
    protected boolean isUsingServices() {
        return false;
    }

    @Override
    protected void adjustArchetype(Properties sysProp) {
        sysProp.put("example", "false");
    }

    @Override
    protected boolean assertPlatformServicesEmpty(File dir) throws IOException {
        StringBuilder sb = new StringBuilder();
        File dialogs = findDialogs(dir, sb);
        assertNotNull(dialogs, "PlatformServices file found");
        String text = readFile(dialogs);
        assertEquals(text.indexOf("confirmByUser"), -1, "Do method confirmByUser in the file: " + dialogs);
        assertEquals(text.indexOf("screenSize"), -1, "Do method screenSize in the file: " + dialogs);
        assertEquals(sb.length(), 0, sb.toString());
        return true;
    }

    private File findDialogs(File root, StringBuilder sb) throws IOException {
        if (root.isDirectory()) {
            File found = null;
            for (File f : root.listFiles()) {
                File r = findDialogs(f, sb);
                if (r != null && found == null) {
                    found = r;
                }
            }
            return found;
        } else {
            if (root.getName().equals("PlatformServices.java")) {
                return root;
            }
            if (root.getName().endsWith(".java")) {
                String text = Files.readFile(root);
                if (text.contains("PlatformServices")) {
                    sb.append("No PlatformServices shall be used ").append(root).append("\n");
                }
            }
            return null;
        }
    }

    @Override
    protected void assertPresenter(File created, Verifier v, String option, String presenter) throws VerificationException {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // skip the check as: Failed to delete
            // maven-archetypes\ko-archetype-test\target\tests\fxcompile\VerifyNoExampleIT-o-a-test\client\target\generated-sources\annotations\org\someuserf5107982\test\oat7\Data.java
            return;
        } else {
            super.assertPresenter(created, v, option, presenter);
        }
    }
}
