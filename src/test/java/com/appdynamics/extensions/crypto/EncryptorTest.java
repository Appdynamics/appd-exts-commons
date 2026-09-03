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

    private final String usageOutput = "usage: java -cp <monitoring-extension.jar> com.appdynamics.extensions.crypto.Encryptor <myEncryptionKey> <myClearTextPassword>    (or)\n" +
            "usage: java -Dappdynamics.agent.monitors.encryptionKey=<myEncryptionKey> -Dappdynamics.agent.monitors.plainText=<myClearTextPassword> -cp <monitoring-extension.jar> com.appdynamics.extensions.crypto.Encryptor\n";

    private final String encryptedOutputHeader = "****************************Encrypted Text**************************";
    private final String encryptedOutputFooter = "********************************************************************";

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
        String[] args = new String[]{"encryptionKey", "plainText"};
        Encryptor.main(args);
        String output = newOutputContent.toString();
        Assert.assertTrue(output.contains(encryptedOutputHeader));
        Assert.assertTrue(output.contains(encryptedOutputFooter));
        // Round-trip verify: extract the ciphertext line and decrypt it
        String ciphertext = output.trim().split("\n")[1].trim();
        Assert.assertEquals("plainText", new com.appdynamics.extensions.crypto.Decryptor("encryptionKey").decrypt(ciphertext));
    }

    @Test
    public void whenArgumentsNotPassedAndSysPropsSetShouldPrintEncryptedTextOutput() {
        setSysProps();
        Encryptor.main(null);
        String output = newOutputContent.toString();
        Assert.assertTrue(output.contains(encryptedOutputHeader));
        Assert.assertTrue(output.contains(encryptedOutputFooter));
        String ciphertext = output.trim().split("\n")[1].trim();
        Assert.assertEquals("plainText", new com.appdynamics.extensions.crypto.Decryptor("encryptionKey").decrypt(ciphertext));
        resetSysProps();
    }

    @Test
    public void whenArgumentsPassedAndSysPropsSetShouldPrintEncryptedTextOutputForTheArguments() {
        setSysProps();
        String[] args = new String[]{"encryptionKey", "plainText2"};
        Encryptor.main(args);
        String output = newOutputContent.toString();
        Assert.assertTrue(output.contains(encryptedOutputHeader));
        Assert.assertTrue(output.contains(encryptedOutputFooter));
        String ciphertext = output.trim().split("\n")[1].trim();
        Assert.assertEquals("plainText2", new com.appdynamics.extensions.crypto.Decryptor("encryptionKey").decrypt(ciphertext));
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
