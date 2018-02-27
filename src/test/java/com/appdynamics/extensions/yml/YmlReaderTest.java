/*
 * Copyright (c) 2018 AppDynamics,Inc.
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

package com.appdynamics.extensions.yml;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

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

    public static void main(String[] args) {
        Map<String, ?> stringMap = YmlReader.readFromFile(new File("/Users/abey.tom/tmp/Junk/farhan/config.yaml"));
        System.out.println(stringMap);
    }
}


