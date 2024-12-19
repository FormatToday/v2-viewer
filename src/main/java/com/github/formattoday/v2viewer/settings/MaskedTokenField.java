package com.github.formattoday.v2viewer.settings;

import com.intellij.ui.components.JBTextField;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * 自定义的带掩码的Token输入框
 */
public class MaskedTokenField extends JBTextField {
    private static final int VISIBLE_CHARS = 4;
    private final MaskedDocument maskedDocument;

    public MaskedTokenField(String text) {
        maskedDocument = new MaskedDocument();
        setDocument(maskedDocument);
        setText(text);
    }

    @Override
    public String getText() {
        return maskedDocument.getActualText();
    }

    @Override
    public void setText(String t) {
        maskedDocument.setActualText(t);
    }

    private class MaskedDocument extends PlainDocument {
        private String actualText = "";

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            actualText = new StringBuilder(actualText).insert(offs, str).toString();
            super.remove(0, getLength());
            super.insertString(0, getMaskedText(), a);
        }

        @Override
        public void remove(int offs, int len) throws BadLocationException {
            actualText = new StringBuilder(actualText).delete(offs, offs + len).toString();
            super.remove(0, getLength());
            super.insertString(0, getMaskedText(), null);
        }

        private String getMaskedText() {
            if (actualText.isEmpty()) return "";
            if (actualText.length() <= VISIBLE_CHARS * 2) return actualText;

            return actualText.substring(0, VISIBLE_CHARS) +
                    "*".repeat(actualText.length() - VISIBLE_CHARS * 2) +
                    actualText.substring(actualText.length() - VISIBLE_CHARS);
        }

        public String getActualText() {
            return actualText;
        }

        public void setActualText(String text) {
            try {
                actualText = text;
                super.remove(0, getLength());
                super.insertString(0, getMaskedText(), null);
            } catch (BadLocationException e) {
                // ignore
            }
        }
    }
}
