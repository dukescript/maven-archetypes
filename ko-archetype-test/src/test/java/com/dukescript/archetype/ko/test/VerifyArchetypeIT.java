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

import com.dukescript.archetype.testing.VerifyBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import org.w3c.dom.Document;

/**
 *
 * @author Anton Epple
 */
public class VerifyArchetypeIT extends VerifyBase {

    public VerifyArchetypeIT() {
        super("knockout4j-archetype");
    }


    @Test public void defaultProjectCompiles() throws Exception {
        final File dir = new File("target/tests/fxcompile/").getAbsoluteFile();
        File created = generateFromArchetype("o-a-test", dir);

        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        Verifier v = createVerifier(created.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("verify");

        v.verifyErrorFreeLog();

        for (String l : v.loadFile(v.getBasedir(), v.getLogFileName(), false)) {
            if (l.contains("j2js")) {
                fail("No pre-compilaton:\n" + l);
            }
            if (l.contains("-javafx.zip")) {
                fail("Don't generate the ZIP by default: " + l);
            }
        }

        v = createVerifier(created.getAbsolutePath());
        v.addCliOption("-Pdesktop");
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoals(Arrays.asList("clean", "install"));

        v.verifyErrorFreeLog();

        final String t = "o-a-test-1.0-SNAPSHOT-javafx.zip";
        verifyFileInLog(v, t);

        final File webpages = new File(new File(new File(created, "client"), "target"), getClass().getSimpleName() + "-o-a-test-1.0-SNAPSHOT-webpages.zip");
        assertTrue(webpages.exists(), "Web pages file created: " + webpages);
        JarFile jf = new JarFile(webpages);
        ZipEntry indexHTML = jf.getEntry("index.html");
        assertNotNull(indexHTML, "index.html in ZIP found");
        InputStream is = jf.getInputStream(indexHTML);
        assertHTMLContent(is);
        assertPlatformServicesEmpty(dir);
        is.close();
        jf.close();

        File dataModel = new File(new File(new File(new File(new File(new File(new File(new File(new File(
            created, "client"), "src"), "main"), "java"), "org"), someuser), "test"), "" + oat + ""), "DataModel.java"
        );
        assertTrue(dataModel.isFile(), "Java file exists: " + dataModel);
        String mainSrc = Files.readFile(dataModel);
        int bootMethod = mainSrc.lastIndexOf("onPageLoad(");
        assertNotEquals(bootMethod, -1, "onPageLoad method present: " + mainSrc);
        int bootMethodEnd = mainSrc.indexOf("}", bootMethod);
        assertNotEquals(bootMethodEnd, -1, "onPageLoad method present: " + mainSrc);

        StringBuilder mainSb = new StringBuilder(mainSrc);
        mainSb.insert(bootMethodEnd, "\n"
          +  "ClassLoader loader = Main.class.getClassLoader();\n"
          +  "if (loader == ClassLoader.getSystemClassLoader()) {\n"
          + "  System.out.println(\"Presenter: \" + org.netbeans.html.boot.spi.Fn.activePresenter().getClass().getName());\n"
          + "  System.exit(0);\n"
          + "} else {\n"
          + "  throw new IllegalStateException(\"wrong classloader:\" + loader);\n"
          + "}\n"
        );

        FileWriter w = new FileWriter(dataModel);
        w.write(mainSb.toString());
        w.close();

        assertPresenter(created, v, "-Pdesktop", "org.netbeans.html.boot.fx.FXPresenter");
        assertPresenter(created, v, "-Pwebkit-presenter", "org.netbeans.html.presenters.webkit.WebKitPresenter");

        if (isJDK11Plus() && System.getProperty("os.name").contains("Mac")) {
            throw new SkipException("Browser presenter 1.5.2 doesn't run on Mac and JDK11");
        }
        assertPresenter(created, v, "-Pbrowser-presenter", "org.netbeans.html.presenters.spi.ProtoPresenterBuilder$GenPresenterWithExecutor");
    }

    private void assertPresenter(File created, Verifier v, String option, String presenter) throws VerificationException {
        Verifier v3 = createVerifier(new File(created.getAbsoluteFile(), "client").getPath());
        v3.localRepo = v.localRepo;
        if (option != null) {
            v3.addCliOption(option);
        }
        v3.executeGoals(Arrays.asList("process-classes", "exec:exec"));

        for (String l : v3.loadFile(v3.getBasedir(), v3.getLogFileName(), false)) {
            if (l.startsWith("Presenter: ")) {
                assertTrue(l.contains(presenter), "Right presenters is used in " + l);
                return;
            }
        }
        fail("No line found in " + v3.getBasedir() + "/" + v3.getLogFileName());
    }

    private void verifyFileInLog(Verifier v, final String t) throws VerificationException {
        v.verifyTextInLog(t.replace('/', File.separatorChar));
    }

    @Test public void iosProjectCompiles() throws Exception {
        final File dir = new File("target/tests/icompile/").getAbsoluteFile();
        File created = generateFromArchetype("o-b-test", dir, "-Diospath=client-ios");

        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        File dataModel = new File(new File(new File(new File(new File(new File(new File(new File(new File(
            created, "client"), "src"), "main"), "java"), "org"), someuser), "test"), "" + oat + ""), "DataModel.java"
        );
        assertTrue(dataModel.isFile(), "Java file exists: " + dataModel);
        String mainSrc = Files.readFile(dataModel);
        int bootMethod = mainSrc.lastIndexOf("onPageLoad(");
        assertNotEquals(bootMethod, -1, "onPageLoad method present: " + mainSrc);
        int bootMethodEnd = mainSrc.indexOf("}", bootMethod);
        assertNotEquals(bootMethodEnd, -1, "onPageLoad method present: " + mainSrc);

        StringBuilder mainSb = new StringBuilder(mainSrc);
        mainSb.insert(bootMethodEnd, "System.exit(0);");

        FileWriter w = new FileWriter(dataModel);
        w.write(mainSb.toString());
        w.close();

        Verifier v = createVerifier(created.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("install");

        v.verifyErrorFreeLog();

        File client = new File(created, "client-ios");
        File useIos = new File(new File(new File(new File(client, "src"), "main"), "java"), "Test.java");
        w = new FileWriter(useIos);
        w.append("class Test {\n");
        w.append("  static Object webView = org.robovm.apple.uikit.UIWebView.class;\n");
        w.append("  static Object natObj = org.robovm.rt.bro.NativeObject.class;\n");
        w.append("  static Object objC = org.robovm.objc.ObjCObject.class;\n");
        w.append("}\n");
        w.close();
        assertTrue(client.isDirectory(), "Subproject dir found: " + client);
        Verifier v2 = createVerifier(client.getAbsolutePath());
        v2.getCliOptions().add("-Denforcer.fail=true");
        try {
            v2.executeGoals(Arrays.asList("package", "robovm:iphone-sim"));
        } catch (VerificationException ex) {
            // OK, the run should fail on other systems than mac
        }
        v2.verifyTextInLog("Building RoboVM app for: ios (x86");

        File nbactions = new File(client, "nbactions.xml");
        assertTrue(nbactions.isFile(), "Actions file is in there");
        assertTrue(Files.readFile(nbactions).contains("robovm"), "There should robovm goals in " + nbactions);

        v2.assertFilePresent("target/images/Icon.png");
        v2.assertFilePresent("target/images/Icon@2.png");
        v2.assertFilePresent("target/images/Icon-60.png");
        v2.assertFilePresent("target/images/Icon-60@2.png");
        v2.assertFilePresent("target/images/Icon-72.png");
        v2.assertFilePresent("target/images/Icon-76.png");
        v2.assertFilePresent("target/images/Default.png");
        v2.assertFilePresent("target/images/Default@2x.png");
        v2.assertFilePresent("target/images/Default-568h@2x.png");
        v2.assertFilePresent("target/images/Default-Landscape.png");
        v2.assertFilePresent("target/images/Default@2x-Landscape.png");
        v2.assertFilePresent("target/images/Default-568h@2x-Landscape.png");
    }

    @Test
    public void moeProjectCompiles() throws Exception {
        if (Boolean.getBoolean("skipMoe")) {
            throw new SkipException("Not executing moe test");
        }
        final File dir = new File("target/tests/moecompile/").getAbsoluteFile();
        File created = generateFromArchetype("o-b-test", dir, "-Dmoepath=client-moe");

        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        File dataModel = new File(new File(new File(new File(new File(new File(new File(new File(new File(
            created, "client"), "src"), "main"), "java"), "org"), someuser), "test"), "" + oat + ""), "DataModel.java"
        );
        assertTrue(dataModel.isFile(), "Java file exists: " + dataModel);
        String mainSrc = Files.readFile(dataModel);
        int bootMethod = mainSrc.lastIndexOf("onPageLoad(");
        assertNotEquals(bootMethod, -1, "onPageLoad method present: " + mainSrc);
        int bootMethodEnd = mainSrc.indexOf("}", bootMethod);
        assertNotEquals(bootMethodEnd, -1, "onPageLoad method present: " + mainSrc);

        StringBuilder mainSb = new StringBuilder(mainSrc);
        mainSb.insert(bootMethodEnd, "System.exit(0);");

        FileWriter w = new FileWriter(dataModel);
        w.write(mainSb.toString());
        w.close();

        Verifier v = createVerifier(created.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("install");

        verifyErrorFreeLogSkipJavaSupport(v);

        File client = new File(created, "client-moe");
        File useIos = new File(new File(new File(new File(client, "src"), "main"), "java"), "Test.java");
        w = new FileWriter(useIos);
        w.append("class Test {\n");
        w.append("  static Object webView = apple.uikit.UIWebView.class;\n");
        w.append("  static Object natObj = org.moe.natj.general.ann.RegisterOnStartup.class;\n");
        w.append("  static Object objC = org.moe.natj.objc.ann.ObjCClassName.class;\n");
        w.append("}\n");
        w.close();
        assertTrue(client.isDirectory(), "Subproject dir found: " + client);
        Verifier v2 = createVerifier(client.getAbsolutePath());
        v2.getCliOptions().add("-Denforcer.fail=true");
        try {
            v2.executeGoals(Arrays.asList("package", "pre-site"));
        } catch (VerificationException ex) {
            v2.verifyTextInLog("Set -Dmoe.launcher.simulators property to ID");
        }
        try {
            v2.executeGoals(Arrays.asList("package", "moe:launch"));
            v2.verifyTextInLog(":moeLaunch");
        } catch (VerificationException ex) {
            // OK, the run should fail on other systems than mac
        }
        v2.verifyTextInLog(":moeGenerateUIObjCInterfaces");

        File nbactions = new File(client, "nbactions.xml");
        assertTrue(nbactions.isFile(), "Actions file is in there");
        final String nbactionsContent = Files.readFile(nbactions);
        assertTrue(nbactionsContent.contains("pre-site"), "Invoke verification of simulators in " + nbactions);
        assertTrue(nbactionsContent.contains("moe:launch"), "There should be moe goals in " + nbactions);

        File xcodePrj = new File(new File(new File(client, "xcode"), "ios.xcodeproj"), "project.pbxproj");
        assertTrue(xcodePrj.isFile());
        String xCodeContent = Files.readFile(xcodePrj);
        Matcher matcher = Pattern.compile("PRODUCT_([A-Z_]+) *= *([^;]*);").matcher(xCodeContent);
        int matchedCount = 0;
        while (matcher.find()) {
            matchedCount++;
            String type = matcher.group(1);
            String value = matcher.group(2);
            switch (type) {
                case "BUNDLE_IDENTIFIER":
                    assertNotEquals(value.indexOf("org." + someuser + ".test"), -1, "value for " + type + " is " + value);
                    break;
                case "NAME":
                    assertNotEquals(value.indexOf("o-b-test"), -1, "value for " + type + " is " + value);
                    break;
                default:
                    fail("Unknown PRODUCT_" + type);
            }
        }
        assertEquals(matchedCount, 8, "4x2 values  found");
    }

    @Test public void iosVerifyRoboVMPlugin() throws Exception {
        final File dir = new File("target/tests/icompilecheck/").getAbsoluteFile();
        File created = generateFromArchetype("x-v-test", dir, "-Diospath=ios-client");

        assertTrue(created.isDirectory(), "Project created");
        final File pom = new File(created, "pom.xml");
        assertTrue(pom.isFile(), "Pom file is in there");

        File client = new File(created, "ios-client");
        assertTrue(client.isDirectory(), "Subproject dir found: " + client);

        final File eff = new File(client, "eff.xml");

        {
            Verifier v = createVerifier(client.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.executeGoal("install");
        }
        {
            Verifier v = createVerifier(client.getAbsolutePath());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-Doutput=" + eff);
            v.executeGoal("help:effective-pom");
        }

        assertTrue(eff.isFile(), "effective pom created: " + eff);

        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(eff);

        final XPathFactory fact = XPathFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        XPathExpression xp = fact.newXPath().compile("//groupId[text() = 'org.robovm']/../version/text()");
        String prev = xp.evaluate(dom);
        assertNotNull(prev, "Plugin version must be found");

        Verifier d = createVerifier(client.getAbsolutePath());
        d.getCliOptions().add("-Denforcer.fail=true");
        d.addCliOption("-X");
        d.executeGoal("dependency:tree");

        File out = new File(new File(d.getBasedir()), d.getLogFileName());

        Pattern p = Pattern.compile(".DEBUG.*com\\.mobidevelop\\.robovm:robo.*:([0-9\\.a-z\\-]*):.*");
        BufferedReader r = new BufferedReader(new FileReader(out));
        int cnt = 0;
        for (;;) {
            String l = r.readLine();
            if (l == null) {
                break;
            }
            Matcher m = p.matcher(l);
            if (!m.matches()) {
                continue;
            }
            int commonLen = Math.min(m.group(1).length(), prev.length());
            String plug = prev.substring(0, commonLen);
            String dep = m.group(1).substring(0, commonLen);

            assertEquals(dep, plug, "Versions must be the same");
            cnt++;
        }
        r.close();
        if (cnt == 0) {
            fail("There should be a RoboVM dependency in " + out);
        }
    }

    @Test public void skipiosProjectCompiles() throws Exception {
        final File dir = new File("target/tests/noicompile/").getAbsoluteFile();
        File generated = generateFromArchetype("m-n-test", dir, "-Diospath=target/skip");

        File created = new File(generated, "client");
        assertTrue(created.isDirectory(), "Project created");
        File pom = new File(created, "pom.xml");
        assertTrue(pom.isFile(), "Pom file is in there");
        assertFalse(Files.readFile(pom).contains("ios"), "There should be no mention of ios in " + pom);

        Verifier v = createVerifier(created.getParent());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("package");

        v.verifyErrorFreeLog();

        File nbactions = new File(created, "nbactions.xml");
        assertTrue(nbactions.isFile(), "Actions file is in there");
        assertFalse(Files.readFile(nbactions).contains("robovm"), "There should be no mention of robovm in " + nbactions);
    }

    @Test public void androidProjectCompiles() throws Exception {
        final File dir = new File("target/tests/androidcmp/").getAbsoluteFile();
        File generated = generateFromArchetype("d-l-test", dir, "-Dandroidpath=android-test");

        File created = new File(generated, "client");
        assertTrue(created.isDirectory(), "Project created");
        final File clientPom = new File(created, "pom.xml");
        assertTrue(clientPom.isFile(), "Pom file is in there");

        File index = new File(new File(new File(new File(new File(
            created, "src"), "main"), "webapp"), "pages"), "index.html");
        assertTrue(index.exists(), "HTML page found " + index);
        File bin = new File(index.getParentFile(), "index.bin");
        writeBinary(bin);

        File and = new File(generated, "android-test");
        assertTrue(and.isDirectory(), "Project created");
        assertTrue(new File(and, "pom.xml").isFile(), "Pom file is in there");

        String sdk = System.getProperty("android.sdk.path");
        if (sdk == null) {
            throw new SkipException("No android.sdk.path set, skipping the test");
        }

        {
            File main = new File(new File(new File(new File(new File(new File(new File(new File(
                created, "src"), "main"), "java"), "org"), someuser), "test"), "" + oat + ""), "Main.java"
            );
            String mainSrc = Files.readFile(main);
            int bootMethod = mainSrc.indexOf("onPageLoad()");
            StringBuilder mainSb = new StringBuilder(mainSrc.substring(0, bootMethod));
            mainSb.append("" +
"onPageLoad() throws Exception {\n" +
"        java.util.concurrent.Callable<?> run = Main::loonPageLoad;\n" +
"        run.call();\n" +
"    }\n" +
"\n" +
"    private static Object loonPageLoad() throws Exception {\n" +
"        DataModel.onPageLoad(" + (isUsingServices()? "null" : "") + ");\n" +
"        return null;\n" +
"    }\n" +
"\n" +
"}"
            );

            FileWriter w = new FileWriter(main);
            w.write(mainSb.toString());
            w.close();

            String pomSrc = Files.readFile(clientPom);
            pomSrc = assertReplace(pomSrc, "<source>1.7</source>", "<source>1.8</source>");
            pomSrc = assertReplace(pomSrc, "<target>1.7</target>", "<target>1.8</target>");

            w = new FileWriter(clientPom);
            w.write(pomSrc.toString());
            w.close();
        }

        if (isJDK11Plus()) {
            throw new SkipException("Android Maven Plugin doesn't work on JDK11 yet");
        }

        {
            Verifier v = createVerifier(created.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-DskipTests=true");
            v.addCliOption("-Dandroid.sdk.path=" + sdk);
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }

        Verifier v = createVerifier(and.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.addCliOption("-Dandroid.sdk.path=" + sdk);
        v.executeGoal("verify");

        v.verifyErrorFreeLog();

        v.assertFilePresent("target/res/drawable-hdpi/ic_launcher.png");
        v.assertFilePresent("target/res/drawable-mdpi/ic_launcher.png");
        v.assertFilePresent("target/res/drawable-xhdpi/ic_launcher.png");
        v.assertFilePresent("target/res/drawable-xxhdpi/ic_launcher.png");

        Verifier v2 = createVerifier(and.getAbsolutePath());
        v2.getCliOptions().add("-Denforcer.fail=true");
        v2.addCliOption("-Dandroid.sdk.path=" + sdk);
        v2.executeGoals(Arrays.asList("package", "android:deploy", "android:run"));
        v2.verifyTextInLog("android-maven-plugin");

        File apk = new File(new File(and, "target"), getClass().getSimpleName() + "-d-l-test-android-1.0-SNAPSHOT.apk");
        assertTrue(apk.isFile(), "apk has been generated: " + apk);

        JarFile jf = new JarFile(apk);
        final ZipEntry indexHTML = jf.getEntry("assets/pages/index.html");
        assertNotNull(indexHTML, "index.html is included in " + apk);
        assertHTMLContent(jf.getInputStream(indexHTML));
        assertPlatformServicesEmpty(generated);
        ZipEntry indexBin = jf.getEntry("assets/pages/index.bin");
        assertNotNull(indexBin, "binary file found in " + apk);
        assertBinary(jf.getInputStream(indexBin));
        jf.close();
    }

    @Test public void withoutAndroidProjectCompiles() throws Exception {
        final File dir = new File("target/tests/wandroidcmp/").getAbsoluteFile();
        File gen = generateFromArchetype("w-d-test", dir, "-Dandroidpath=target/skip");

        File created = new File(gen, "client");
        assertTrue(created.isDirectory(), "Project created");
        final File pom = new File(created, "pom.xml");
        assertTrue(pom.isFile(), "Pom file is in there");
        assertFalse(Files.readFile(pom).contains("android"), "There should be no mention of android in " + pom);

        {
            Verifier v = createVerifier(created.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-DskipTests=true");
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }

        Verifier v = createVerifier(created.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("verify");

        v.verifyErrorFreeLog();
    }

    @Test
    public void webProjectCompiles() throws Exception {
        final File dir = new File("target/tests/b2bcmp/").getAbsoluteFile();
        File gen = generateFromArchetype("b-p-test", dir, "-Dwebpath=test-web");

        File created = new File(gen, "client");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        File main = new File(new File(created, "src"), "main");
        File pages = new File(new File(main, "webapp"), "pages");
        File index = new File(pages, "index.html");
        assertTrue(index.exists(), "HTML page found " + index);
        File bin = new File(index.getParentFile(), "index.bin");
        writeBinary(bin);

        File web = new File(gen, "test-web");
        assertTrue(web.isDirectory(), "Project created");
        assertTrue(new File(web, "pom.xml").isFile(), "Pom file is in there");

        String indexContent = Files.readFile(index);
        assertTrue(indexContent.contains("${browser.bootstrap}"), "There should be bck2brwsr.js placeholder in " + index);

        File jsDir = new File(gen, "js");
        assertTrue(jsDir.isDirectory(), "Directory is found");

        File jsFile = new File(new File(new File(new File(new File(new File(new File(new File(new File(jsDir, "src"), "main"), "java"), "org"), someuser), "test"), "" + oat + ""), "js"), "PlatformServices.java");
        assertTrue(jsFile.isFile(), "File found");

        String jsCode = Files.readFile(jsFile);
        final String replace = "w.innerWidth";
        int where = jsCode.indexOf(replace);
        boolean checkForNonExistingAttr;
        if (where < 0) {
            assertTrue(assertPlatformServicesEmpty(dir));
            checkForNonExistingAttr = false;
        } else {
            jsCode = jsCode.substring(0, where) + "w.reallyNonExistingAttr" + jsCode.substring(where + replace.length());
            FileWriter w = new FileWriter(jsFile);
            w.write(jsCode);
            w.close();
            checkForNonExistingAttr = true;
        }

        {
            Verifier v = createVerifier(created.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-DskipTests=true");
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }

        Verifier v = createVerifier(web.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("package");

        v.verifyErrorFreeLog();
        verifyFileInLog(v, "b-p-test-web-1.0-SNAPSHOT-bck2brwsr.zip");

        v.assertFileNotPresent("target/res/drawable-hdpi/ic_launcher.png");
        v.assertFileNotPresent("target/res/drawable-mdpi/ic_launcher.png");
        v.assertFileNotPresent("target/res/drawable-xhdpi/ic_launcher.png");
        v.assertFileNotPresent("target/res/drawable-xxhdpi/ic_launcher.png");

        v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-p-test-web-1.0-SNAPSHOT-bck2brwsr/");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-p-test-web-1.0-SNAPSHOT-bck2brwsr/public_html/bck2brwsr.js");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-p-test-web-1.0-SNAPSHOT-bck2brwsr.zip");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-p-test.js");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-p-test-web-1.0-SNAPSHOT-bck2brwsr/public_html/index.html");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-p-test-web-1.0-SNAPSHOT-bck2brwsr/public_html/index.bin");
        File genRoot = new File(new File(new File(web, "target"), getClass().getSimpleName() + "-b-p-test-web-1.0-SNAPSHOT-bck2brwsr"), "public_html");
        File indexBin = new File(genRoot, "index.bin");
        assertTrue(indexBin.exists(), "index.bin really exists");
        assertBinary(new FileInputStream(indexBin));

        File genJSLib = new File(new File(genRoot, "lib"), getClass().getSimpleName() + "-b-p-test-js-1.0-SNAPSHOT.js");
        assertTrue(genJSLib.exists(), "JsLib file found: " + genJSLib);
        if (checkForNonExistingAttr) {
            String genJSCode = Files.readFile(genJSLib);
            assertTrue(genJSCode.contains("w.reallyNonExistingAttr"), "w.reallyNonExistingAttr found in\n" + genJSCode);
        }

        File indexGen = new File(genRoot, "index.html");
        String indexGenContent = Files.readFile(indexGen);
        assertTrue(indexGenContent.contains("src=\"bck2brwsr.js\""), "There should be bck2brwsr.js reference in " + indexGen);

        File nbactions = new File(web, "nbactions.xml");
        assertTrue(nbactions.isFile(), "Actions file is in there");
        assertTrue(Files.readFile(nbactions).contains("bck2brwsr"), "There should bck2brwsr goal in " + nbactions);

        for (String line : v.loadFile(v.getBasedir(), v.getLogFileName(), false)) {
            if (line.matches(".*Generating.*emul.*")) {
                fail("Don't generate emul: " + line);
            }
            if (line.matches(".*Generating.*net.java.html.*")) {
                fail("Don't generate HTML/Java libraries: " + line);
            }
        }

        assertNoTextInSubdir("boot.fx", genRoot);
    }

    @Test
    public void teaVMwebProjectCompiles() throws Exception {
        final File dir = new File("target/tests/teavmcmp/").getAbsoluteFile();
        File gen = generateFromArchetype("t-p-test", dir, "-Dwebpath=test-web");

        File created = new File(gen, "client");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        File main = new File(new File(created, "src"), "main");
        File pages = new File(new File(main, "webapp"), "pages");
        File index = new File(pages, "index.html");
        assertTrue(index.exists(), "HTML page found " + index);
        File bin = new File(index.getParentFile(), "index.bin");
        writeBinary(bin);

        File web = new File(gen, "test-web");
        assertTrue(web.isDirectory(), "Project created");
        assertTrue(new File(web, "pom.xml").isFile(), "Pom file is in there");

        String indexContent = Files.readFile(index);
        assertTrue(indexContent.contains("${browser.bootstrap}"), "There should be teavm.js placeholder in " + index);

        if (System.getProperty("java.version").startsWith("1.7")) {
            throw new SkipException("TeaVM doesn't run on 1.7");
        }

        {
            Verifier v = createVerifier(created.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-Pteavm");
            v.addCliOption("-DskipTests=true");
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }

        Verifier v = createVerifier(web.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.addCliOption("-Pteavm");
        v.executeGoals(Arrays.asList("clean", "package"));

        v.verifyErrorFreeLog();
        verifyFileInLog(v, "t-p-test-web-1.0-SNAPSHOT-teavm.zip");

        v.assertFilePresent("target/" + getClass().getSimpleName() + "-t-p-test-web-1.0-SNAPSHOT-teavm/");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-t-p-test-web-1.0-SNAPSHOT-teavm/public_html/teavm.js");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-t-p-test-web-1.0-SNAPSHOT-teavm.zip");
// bck2brwsr:aot should not be executed when TeaVM is on:
//        v.assertFileNotPresent("target/" + getClass().getSimpleName() + "-t-p-test.js");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-t-p-test-web-1.0-SNAPSHOT-teavm/public_html/index.html");
        v.assertFilePresent("target/" + getClass().getSimpleName() + "-t-p-test-web-1.0-SNAPSHOT-teavm/public_html/index.bin");
        File genRoot = new File(new File(new File(web, "target"), getClass().getSimpleName() + "-t-p-test-web-1.0-SNAPSHOT-teavm"), "public_html");
        File indexBin = new File(genRoot, "index.bin");
        assertTrue(indexBin.exists(), "index.bin really exists");
        assertBinary(new FileInputStream(indexBin));

        File indexGen = new File(genRoot, "index.html");
        String indexGenContent = Files.readFile(indexGen);
        assertTrue(indexGenContent.contains("src=\"teavm.js\""), "There should be bck2brwsr.js reference in " + indexGen);

        File nbactions = new File(web, "nbactions.xml");
        assertTrue(nbactions.isFile(), "Actions file is in there");
        assertTrue(Files.readFile(nbactions).contains("teavm"), "There should teavm goal in " + nbactions);

        for (String line : v.loadFile(v.getBasedir(), v.getLogFileName(), false)) {
            if (line.matches(".*Generating.*emul.*")) {
                fail("Don't generate emul: " + line);
            }
            if (line.matches(".*Generating.*net.java.html.*")) {
                fail("Don't generate HTML/Java libraries: " + line);
            }
        }

        assertNoTextInSubdir("boot.fx", genRoot);
    }

    @Test public void bck2brwsrAndNbrwsrProjectCompiles() throws Exception {
        final File dir = new File("target/tests/BandN/").getAbsoluteFile();
        File gen = generateFromArchetype("b-n-test", dir, "-Dwebpath=for-web", "-Dnetbeanspath=for-nb");

        final File created = new File(gen, "client");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        final File forWeb = new File(gen, "for-web");
        assertTrue(forWeb.isDirectory(), "Web Project created");
        assertTrue(new File(forWeb, "pom.xml").isFile(), "Pom file is in there");

        File main = new File(new File(created, "src"), "main");
        File pages = new File(new File(main, "webapp"), "pages");
        File index = new File(pages, "index.html");

        String indexContent = Files.readFile(index);
        assertTrue(indexContent.contains("${browser.bootstrap}"), "There should be bck2brwsr.js placeholder in " + index);

        {
            Verifier v = createVerifier(created.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-DskipTests=true");
            v.addCliOption("-Dbck2brwsr.obfuscationlevel=NONE");
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }

        {
            Verifier v = createVerifier(forWeb.getAbsolutePath());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.executeGoal("package");

            v.verifyErrorFreeLog();
            verifyFileInLog(v, "b-n-test-web-1.0-SNAPSHOT-bck2brwsr.zip");

            v.assertFileNotPresent("target/res/drawable-hdpi/ic_launcher.png");
            v.assertFileNotPresent("target/res/drawable-mdpi/ic_launcher.png");
            v.assertFileNotPresent("target/res/drawable-xhdpi/ic_launcher.png");
            v.assertFileNotPresent("target/res/drawable-xxhdpi/ic_launcher.png");

            v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-n-test-web-1.0-SNAPSHOT-bck2brwsr/");
            v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-n-test-web-1.0-SNAPSHOT-bck2brwsr/public_html/bck2brwsr.js");
            v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-n-test-web-1.0-SNAPSHOT-bck2brwsr.zip");
            v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-n-test.js");
            v.assertFilePresent("target/" + getClass().getSimpleName() + "-b-n-test-web-1.0-SNAPSHOT-bck2brwsr/public_html/index.html");

            File genRoot = new File(new File(new File(forWeb, "target"), getClass().getSimpleName() + "-b-n-test-web-1.0-SNAPSHOT-bck2brwsr"), "public_html");
            File indexGen = new File(genRoot, "index.html");
            String indexGenContent = Files.readFile(indexGen);
            assertTrue(indexGenContent.contains("src=\"bck2brwsr.js\""), "There should be bck2brwsr.js reference in " + indexGen);

            File nbactions = new File(forWeb, "nbactions.xml");
            assertTrue(nbactions.isFile(), "Actions file is in there");
            final String cntnt = Files.readFile(nbactions);
            assertTrue(cntnt.contains("bck2brwsr"), "There should bck2brwsr goal in " + nbactions);
            assertTrue(cntnt.contains("CUSTOM-bck2brwsr-web"), "An action to generate a web in " + nbactions);
        }

        {
            final File forNb = new File(gen, "for-nb");
            assertTrue(forNb.isDirectory(), "Nb Project created");
            assertTrue(new File(forNb, "pom.xml").isFile(), "Pom file is in there");

            Verifier v = createVerifier(forNb.getAbsolutePath());
            v.executeGoals(Arrays.asList("package", "nbm:cluster"));

            final File netbeans = new File(new File(forNb, "target"), "netbeans");
            assertTrue(netbeans.isDirectory(), netbeans + " is in there");

            final String[] sharedVersion = { null };
            java.nio.file.Files.walkFileTree(netbeans.toPath(), new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".jar")) {
                        JarFile jf = new JarFile(file.toFile());
                        final Attributes mainAttributes = jf.getManifest().getMainAttributes();
                        String name = mainAttributes.getValue("Bundle-SymbolicName");
                        if (name != null && name.contains("html")) {
                            String version = mainAttributes.getValue("Bundle-Version");
                            if (sharedVersion[0] == null) {
                                sharedVersion[0] = version;
                            } else {
                                assertEquals(version, sharedVersion[0], "Proper version for " + file.getFileName());
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    fail("Cannot visit " + file);
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Test
    public void nbrwsrProjectCompiles() throws Exception {
        final File dir = new File("target/tests/ncmp/").getAbsoluteFile();
        File gen = generateFromArchetype("n-p-test", dir, "-Dnetbeanspath=nb-test");

        File created = new File(gen, "client");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        File nb = new File(gen, "nb-test");
        assertTrue(nb.isDirectory(), "Project created");
        assertTrue(new File(nb, "pom.xml").isFile(), "Pom file is in there");

        File main = new File(new File(created, "src"), "main");
        File pages = new File(new File(main, "webapp"), "pages");
        File index = new File(pages, "index.html");
        assertTrue(index.exists(), "Index page is there");

        File launcher = new File(new File(new File(new File(nb, "src"), "main"), "icons"), "launcher.png");
        assertTrue(launcher.exists(), "Icon is there: " + launcher);

        File plus = new File(pages, "plus.css");
        plus.createNewFile();

        {
            Verifier v = createVerifier(nb.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.addCliOption("-DskipTests=true");
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }


        {
            Verifier v = createVerifier(nb.getAbsolutePath());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.executeGoal("package");

            v.verifyErrorFreeLog();

            v.assertFilePresent("target/classes/META-INF/generated-layer.xml");
            v.assertFilePresent("target/classes/org/" + someuser + "/test/" + oat + "/index.html");
            v.assertFilePresent("target/classes/org/" + someuser + "/test/" + oat + "/plus.css");
        }

        File jar = new File(new File(nb, "target"), getClass().getSimpleName() + "-n-p-test-nb-1.0-SNAPSHOT.jar");
        assertTrue(jar.exists(), "File is created: " + jar);
        JarFile jf = new JarFile(jar);
        String cp = jf.getManifest().getMainAttributes().getValue("Class-Path");
        assertNull(cp, "Classpath found: " + cp);

        File nbactions = new File(nb, "nbactions.xml");
        assertTrue(nbactions.isFile(), "Actions file is in there");
        final String nbActionsContent = Files.readFile(nbactions);
        assertTrue(nbActionsContent.contains("nbm"), "There should nbm goal in " + nbactions);
        assertTrue(nbActionsContent.contains("nbm:cluster"), "There should nbm:cluster goal in " + nbactions);
        assertTrue(nbActionsContent.contains("nbm:run-platform"), "There should nbm:run-platform goal in " + nbactions);

        {
            File closeJava = new File(new File(new File(new File(new File(
                new File(new File(new File(nb, "src"), "main"), "java"),
                "org"), someuser), "test"), "" + oat + ""),"CloseTestApp.java"
            );
            FileWriter w = new FileWriter(closeJava);
            w.write(
"package org." + someuser + ".test." + oat + ";\n" +
"import java.lang.reflect.Method;\n" +
"import org.openide.windows.OnShowing;\n" +
"\n" +
"@OnShowing\n" +
"public final class CloseTestApp implements Runnable {\n" +
"    @Override\n" +
"    public void run() {\n" +
"        try {\n" +
"            Class<?> lm = Class.forName(\"org.openide.LifecycleManager\");\n" +
"            Method gd = lm.getMethod(\"getDefault\");\n" +
"            Method ex = lm.getMethod(\"exit\", int.class);\n" +
"            Object lmInst = gd.invoke(null);\n" +
"            ex.invoke(lmInst, 0);\n" +
"        } catch (Exception ex) {\n" +
"            throw new IllegalStateException(ex);\n" +
"        }\n" +
"    }\n" +
"}\n" +
"\n"
            );
            w.close();

            final List<String>  finalGoals = Arrays.asList("package", "nbm:cluster", "nbm:run-platform");

            Verifier v = createVerifier(nb.getAbsolutePath());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.executeGoals(finalGoals);
        }
    }

    @Test
    public void nbrwsrProjectCompilesForNetBeansAndCopiesAllResources() throws Exception {
        final File dir = new File("target/tests/nbmallres/").getAbsoluteFile();
        File gen = generateFromArchetype("a-r-test", dir, "-Dnetbeanspath=test-netbeans");

        File created = new File(gen, "client");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");

        File nb = new File(gen, "test-netbeans");
        assertTrue(nb.isDirectory(), "Project created");
        assertTrue(new File(nb, "pom.xml").isFile(), "Pom file is in there");

        File main = new File(new File(created, "src"), "main");
        File pages = new File(new File(main, "webapp"), "pages");
        File index = new File(pages, "index.html");
        assertTrue(index.exists(), "Index page is there");

        File plus = new File(pages, "plus.css");
        plus.createNewFile();

        {
            Verifier v = createVerifier(created.getParent());
            v.getCliOptions().add("-Denforcer.fail=true");
            v.executeGoal("install");
            v.verifyErrorFreeLog();
        }
        Verifier v = createVerifier(nb.getAbsolutePath());
        v.getCliOptions().add("-Denforcer.fail=true");
        v.executeGoal("install");

        v.verifyErrorFreeLog();

        v.assertFilePresent("target/" + getClass().getSimpleName() + "-a-r-test-nb-1.0-SNAPSHOT.nbm");
        v.assertFilePresent("target/classes/org/" + someuser + "/test/" + oat + "/index.html");
        v.assertFilePresent("target/classes/org/" + someuser + "/test/" + oat + "/plus.css");
        v.assertFilePresent("target/classes/org/" + someuser + "/test/" + oat + "/icon.png");
        v.assertFilePresent("target/classes/org/" + someuser + "/test/" + oat + "/icon24.png");
    }

    protected boolean isUsingServices() {
        return true;
    }

    protected boolean assertPlatformServicesEmpty(File dir) throws IOException {
        return false;
    }
}
