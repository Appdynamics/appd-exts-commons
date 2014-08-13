package com.appdynamics.extensions.yml;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 8/12/14
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class YmlReaderTest {

    @Test
    public void readYML() {
        YmlTestClass1[] values = YmlReader.readFromClasspath("/test.yml", YmlTestClass1[].class);
        Assert.assertEquals(2, values.length);
    }

    @Test(expected = YmlReader.InvalidYmlPathException.class)
    public void readYMLFail() {
        YmlTestClass1[] values = YmlReader.readFromClasspath("    ", YmlTestClass1[].class);
        Assert.assertEquals(2, values.length);
    }


    public static class YmlTestClass1 {
        private String string1;
        private int int1;
        private YmlTestClass2[] innerClasses;
        private YmlTestClass2 innerClass;

        public String getString1() {
            return string1;
        }

        public void setString1(String string1) {
            this.string1 = string1;
        }

        public int getInt1() {
            return int1;
        }

        public void setInt1(int int1) {
            this.int1 = int1;
        }

        public YmlTestClass2[] getInnerClasses() {
            return innerClasses;
        }

        public void setInnerClasses(YmlTestClass2[] innerClasses) {
            this.innerClasses = innerClasses;
        }

        public YmlTestClass2 getInnerClass() {
            return innerClass;
        }

        public void setInnerClass(YmlTestClass2 innerClass) {
            this.innerClass = innerClass;
        }
    }

    public static class YmlTestClass2 {
        private String string2;
        private long long2;

        public String getString2() {
            return string2;
        }

        public void setString2(String string2) {
            this.string2 = string2;
        }

        public long getLong2() {
            return long2;
        }

        public void setLong2(long long2) {
            this.long2 = long2;
        }
    }
}


