package org.kobjects.htmlview2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberTest {

  @Test
  public void latinLower() {
    assertEquals("a", HtmlViewGroup.toLetters(1, 'a', 26));
    assertEquals("z", HtmlViewGroup.toLetters(26, 'a', 26));
    assertEquals("aa", HtmlViewGroup.toLetters(27, 'a', 26));
    assertEquals("az", HtmlViewGroup.toLetters(52, 'a', 26));
    assertEquals("ba", HtmlViewGroup.toLetters(53, 'a', 26));
  }

  @Test
  public void latinUpper() {
    assertEquals("A", HtmlViewGroup.toLetters(1, 'A', 26));
    assertEquals("Z", HtmlViewGroup.toLetters(26, 'A', 26));
    assertEquals("AA", HtmlViewGroup.toLetters(27, 'A', 26));
    assertEquals("AZ", HtmlViewGroup.toLetters(52, 'A', 26));
    assertEquals("BA", HtmlViewGroup.toLetters(53, 'A', 26));
  }

  @Test
  public void greekLower() {
    assertEquals("α", HtmlViewGroup.toLetters(1, 'α', 25));
    assertEquals("ω", HtmlViewGroup.toLetters(25, 'α', 25));
    assertEquals("αα", HtmlViewGroup.toLetters(26, 'α', 25));
    assertEquals("αω", HtmlViewGroup.toLetters(50, 'α', 25));
    assertEquals("βα", HtmlViewGroup.toLetters(51, 'α', 25));
  }

  @Test
  public void roman() {
    assertEquals("I", HtmlViewGroup.toRoman(1));
    assertEquals("II", HtmlViewGroup.toRoman(2));
    assertEquals("III", HtmlViewGroup.toRoman(3));
    assertEquals("IV", HtmlViewGroup.toRoman(4));
    assertEquals("V", HtmlViewGroup.toRoman(5));
    assertEquals("VI", HtmlViewGroup.toRoman(6));
    assertEquals("VII", HtmlViewGroup.toRoman(7));
    assertEquals("VIII", HtmlViewGroup.toRoman(8));
    assertEquals("IX", HtmlViewGroup.toRoman(9));
    assertEquals("X", HtmlViewGroup.toRoman(10));
    assertEquals("XI", HtmlViewGroup.toRoman(11));
    assertEquals("XII", HtmlViewGroup.toRoman(12));
    assertEquals("M", HtmlViewGroup.toRoman(1000));
    assertEquals("MCMLXX", HtmlViewGroup.toRoman(1970));
    assertEquals("MCMXCVIII", HtmlViewGroup.toRoman(1998));
    assertEquals("MM", HtmlViewGroup.toRoman(2000));
    assertEquals("MMXVII", HtmlViewGroup.toRoman(2017));
  }

}