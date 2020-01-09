package de.dfki.grave.util;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.dfki.grave.editor.panels.EditorInstance;

/**
 * @author Gregor Mehlmann
 */
public class TextFormat {

  /**
   *
   */
  public static ArrayList<Pair<String, AttributedString>> getPairList(ArrayList<String> formattedStringList) {
    ArrayList<Pair<String, AttributedString>> pairList = new ArrayList<Pair<String, AttributedString>>();

    for (int i = 0; i < formattedStringList.size(); i++) {
      Pair<String, AttributedString> pair = fillWithAttributes(formattedStringList.get(i));

      pairList.add(pair);
    }

    return pairList;
  }

  /**
   *
   */
  public static Pair<String, AttributedString> fillWithAttributes(String inputString) {
    HashMap<Pair<TextAttribute, Object>, Pair<Integer, Integer>> attributeMap = new HashMap();
    String unformattedString = new String();

    while (true) {
      int i = inputString.indexOf('#');

      if (i == -1) {
        unformattedString = unformattedString.concat(inputString);

        break;
      }

      int j = inputString.indexOf(' ', i);
      int k = 0;
      String subString = "";
      String newInputString = "";

      if (j == -1) {
        k = inputString.length() - 1;

        // Get infix
        subString = inputString.substring(i + 3);
      } else {
        k = j;

        // Get infix
        subString = inputString.substring(i + 3, k);
        newInputString = inputString.substring(k);
      }

      // Get format string
      char c = inputString.charAt(i + 1);

      // Get prefix
      String preString = inputString.substring(0, i);

      // Get postfix
      String postString = inputString.substring(i + 3);

      //
      Pair<Integer, Integer> position
              = new Pair<Integer, Integer>(
                      unformattedString.length() + preString.length(),
                      unformattedString.length() + preString.length() + subString.length());

      if (c == 'b') {
        // Highlight with bold weight
        Pair attribute
                = new Pair(
                        TextAttribute.WEIGHT,
                        TextAttribute.WEIGHT_BOLD);
        attributeMap.put(attribute, position);

      } else if (c == 'i') {
        // Highlight with italic posture
        Pair attribute
                = new Pair(
                        TextAttribute.POSTURE,
                        TextAttribute.POSTURE_OBLIQUE);
        attributeMap.put(attribute, position);
      } else if (c == 'r') {
        // Highlight like reserved word
        Pair attribute1
                = new Pair(
                        TextAttribute.FOREGROUND, Color.BLUE);
        Pair attribute2
                = new Pair(
                        TextAttribute.WEIGHT,
                        TextAttribute.WEIGHT_BOLD);

        attributeMap.put(attribute1, position);
        attributeMap.put(attribute2, position);
      } else if (c == 'c') {

        // Constant Literals
        Pair attribute
                = new Pair(
                        TextAttribute.FOREGROUND,
                        Color.RED.darker());

        attributeMap.put(attribute, position);
      } else if (c == 't') {

        // User-Defined Type Names
        Pair attribute
                = new Pair(
                        TextAttribute.FOREGROUND,
                        Color.MAGENTA.darker());

        attributeMap.put(attribute, position);
      } else if (c == 'h') {

        // Struct or List Definitions
        Pair attribute
                = new Pair(
                        TextAttribute.FOREGROUND, Color.BLACK);

        attributeMap.put(attribute, position);
      } else if (c == 'p') {
        // Highlight predefined commands
        Pair attribute1
                = new Pair(
                        TextAttribute.FOREGROUND,
                        Color.GREEN.darker().darker().darker());
        Pair attribute2
                = new Pair(
                        TextAttribute.WEIGHT,
                        TextAttribute.WEIGHT_BOLD);

        attributeMap.put(attribute1, position);
        attributeMap.put(attribute2, position);
      }

      unformattedString = unformattedString.concat(preString).concat(subString);
      inputString = newInputString;
    }

    AttributedString attributedString = new AttributedString(unformattedString);

    attributedString.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
    attributedString.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    attributedString.addAttribute(TextAttribute.FAMILY, Font.SANS_SERIF);

    if (EditorInstance.getInstance().getSelectedProjectEditor() != null) {
      attributedString.addAttribute(TextAttribute.SIZE, EditorInstance.getInstance().getSelectedProjectEditor().getEditorProject().getEditorConfig().sWORKSPACEFONTSIZE);
    } else {
      attributedString.addAttribute(TextAttribute.SIZE, 12);
    }

    // Fill the attributed string with attributes
    Iterator it = attributeMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      Pair<TextAttribute, Object> key = (Pair<TextAttribute, Object>) entry.getKey();
      Pair<Integer, Integer> value = (Pair<Integer, Integer>) entry.getValue();

      attributedString.addAttribute(key.getFirst(), key.getSecond(), value.getFirst(), value.getSecond());
    }

    return new Pair<String, AttributedString>(unformattedString, attributedString);
  }

  public static String formatConstantStringLiteral(String valueString) {
    ArrayList<Character> charList = new ArrayList<Character>();
    char[] charArray = valueString.toCharArray();

    for (int i = 0; i < charArray.length; i++) {
      if (charArray[i] == ' ') {
        charList.add(charArray[i]);
      } else {
        if (i == 0) {
          charList.add('#');
          charList.add('c');
          charList.add('#');
          charList.add(charArray[i]);
        } else {
          if (charArray[i - 1] == ' ') {
            charList.add('#');
            charList.add('c');
            charList.add('#');
            charList.add(charArray[i]);
          } else {
            charList.add(charArray[i]);
          }
        }
      }
    }

    char[] formattedCharArray = new char[charList.size()];

    for (int i = 0; i < formattedCharArray.length; i++) {
      formattedCharArray[i] = charList.get(i);
    }

    String formatedString = String.valueOf(formattedCharArray);

    return formatedString;
  }
}
