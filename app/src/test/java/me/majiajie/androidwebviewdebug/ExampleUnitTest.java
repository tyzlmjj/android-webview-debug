package me.majiajie.androidwebviewdebug;

import org.junit.Test;

import me.majiajie.androidwebviewdebug.utils.DampingUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
        System.out.println(DampingUtils.getViewMove(50,100,50) + "");
    }
}