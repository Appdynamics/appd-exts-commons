package com.appdynamics.extensions.crypto;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.appdynamics.extensions.SystemPropertyConstants.ENCRYPTION_KEY_PROPERTY;
import static com.appdynamics.extensions.SystemPropertyConstants.PLAIN_TEXT_PROPERTY;

/**
 * Created by venkata.konala on 2/18/19.
 */
public class EncryptorTest {

    private PrintStream oldOutputStream = System.out;

    private ByteArrayOutputStream newOutputContent = new ByteArrayOutputStream();
    private PrintStream newOutputStream = new PrintStream(newOutputContent);

    private final String usageOutput = "usage: java -cp appd-exts-commons-<version>.jar com.appdynamics.extensions.encrypt.Encryptor <myKey> <myPassword>    (or)\n" +
            "usage: java -Dappdynamics.agent.monitors.encryptionKey=<mykey> -Dappdynamics.agent.monitors.plainText=<myPassword> -cp appd-exts-commons-<version>.jar com.appdynamics.extensions.encrypt.Encryptor\n";

    private final String encryptedTextOutput = "****************************Encrypted Text**************************\n" +
            "avQa9cYNOoO6Ba1p3He+HQ==\n" +
            "********************************************************************\n";

    private final String encryptedTextOutput2 = "****************************Encrypted Text**************************\n" +
            "nW3Yi3gzkxzjwEA3J21eOA==\n" +
            "********************************************************************\n";

    @Before
    public void setOutputStream() {
        System.setOut(newOutputStream);
    }

    @After
    public void resetOutputStream() {
        System.setOut(oldOutputStream);
    }

    @Test
    public void whenNoArgumentsPassedAndNoSysPropsSetShouldPrintUsageOutput() {
        Encryptor.main(null);
        Assert.assertTrue(usageOutput.equalsIgnoreCase(newOutputContent.toString()));
    }

    @Test
    public void whenArgumentsPassedAndNoSysPropsSetShouldPrintEncryptedTextOutput() {
        String args[] = new String[] {"encryptionKey", "plainText"};
        Encryptor.main(args);
        Assert.assertTrue(encryptedTextOutput.equalsIgnoreCase(newOutputContent.toString()));
    }

    @Test
    public void whenArgumentsNotPassedAndSysPropsSetShouldPrintEncryptedTextOutput() {
        setSysProps();
        Encryptor.main(null);
        Assert.assertTrue(encryptedTextOutput.equalsIgnoreCase(newOutputContent.toString()));
        resetSysProps();
    }

    @Test
    public void whenArgumentsPassedAndSysPropsSetShouldPrintEncryptedTextOutputForTheArguments() {
        setSysProps();
        String args[] = new String[] {"encryptionKey", "plainText2"};
        Encryptor.main(args);
        Assert.assertTrue(encryptedTextOutput2.equalsIgnoreCase(newOutputContent.toString()));
        resetSysProps();
    }

    private void setSysProps() {
        System.setProperty(ENCRYPTION_KEY_PROPERTY, "encryptionKey");
        System.setProperty(PLAIN_TEXT_PROPERTY, "plainText");
    }

    private void resetSysProps() {
        System.clearProperty(ENCRYPTION_KEY_PROPERTY);
        System.clearProperty(PLAIN_TEXT_PROPERTY);
    }
}
