package com.appdynamics.extensions.encrypt;

/**
 * We shouldn't ideally support multiple encryption keys per machine agent, hence singeton.
 * However this will be loaded by each extension as of now.
 * <p/>
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/16/14
 * Time: 9:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class Encryptor {
    private static Encryptor ourInstance = new Encryptor();

    public static Encryptor getInstance() {
        return ourInstance;
    }

    private Encryptor() {
        //GET the encryption key
        //Validate it
        //Throw Initialization exception.
    }

    public String encrypt(String clearText) {
        //Dummy impl for Testcase
        return clearText.toUpperCase();
    }

    public String decrypt(String encrypted) {
        //Throw runtime exception
        //Dummy impl for Testcase
        return encrypted.toLowerCase();
    }

}
