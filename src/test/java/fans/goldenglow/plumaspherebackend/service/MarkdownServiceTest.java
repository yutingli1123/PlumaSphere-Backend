package fans.goldenglow.plumaspherebackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownServiceTest {
    private MarkdownService markdownService;

    @BeforeEach
    void setUp() {
        markdownService = new MarkdownService();
    }

    @Test
    void testConvertMarkdownToPlainText_SimpleText() {
        // Given
        String markdown = "This is simple text";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("This is simple text", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithHeaders() {
        // Given
        String markdown = "# Header 1\n## Header 2\n### Header 3";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Header 1 Header 2 Header 3", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithBoldAndItalic() {
        // Given
        String markdown = "**Bold text** and *italic text*";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Bold text and italic text", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithLinks() {
        // Given
        String markdown = "[Link text](http://example.com)";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Link text", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithCodeBlocks() {
        // Given
        String markdown = "```java\nSystem.out.println(\"Hello\");\n```";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("System.out.println(\"Hello\");", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithInlineCode() {
        // Given
        String markdown = "Use `System.out.println()` to print";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Use System.out.println() to print", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithLists() {
        // Given
        String markdown = "- Item 1\n- Item 2\n- Item 3";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Item 1 Item 2 Item 3", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithOrderedLists() {
        // Given
        String markdown = "1. First item\n2. Second item\n3. Third item";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("First item Second item Third item", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithBlockquotes() {
        // Given
        String markdown = "> This is a quote\n> Another line";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("This is a quote Another line", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithLineBreaks() {
        // Given
        String markdown = "Line 1\nLine 2\r\nLine 3\rLine 4";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Line 1 Line 2 Line 3 Line 4", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WithMixedContent() {
        // Given
        String markdown = "# Title\n\nThis is **bold** text with *italic* parts.\n\n- List item 1\n- List item 2\n\n[Link](http://example.com)";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Title This is bold text with italic parts. List item 1 List item 2 Link", result);
    }

    @Test
    void testConvertMarkdownToPlainText_EmptyString() {
        // Given
        String markdown = "";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("", result);
    }

    @Test
    void testConvertMarkdownToPlainText_NullInput() {
        // Given
        String markdown = null;

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        // FlexMark parser handles null input gracefully and returns empty string
        assertEquals("", result);
    }

    @Test
    void testConvertMarkdownToPlainText_WhitespaceOnly() {
        // Given
        String markdown = "   \n\t  \r\n  ";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("", result);
    }

    @Test
    void testConvertMarkdownToPlainText_SpecialCharacters() {
        // Given
        String markdown = "Text with special chars: & < > \" '";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Text with special chars: & < > \" '", result);
    }

    @Test
    void testConvertMarkdownToPlainText_Tables() {
        // Given
        String markdown = "| Column 1 | Column 2 |\n|----------|----------|\n| Row 1    | Data 1   |\n| Row 2    | Data 2   |";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertTrue(result.contains("Column 1"));
        assertTrue(result.contains("Column 2"));
        assertTrue(result.contains("Row 1"));
        assertTrue(result.contains("Data 1"));
    }

    @Test
    void testConvertMarkdownToPlainText_StrikeThrough() {
        // Given
        String markdown = "~~strikethrough~~ text";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertTrue(result.contains("strikethrough"));
        assertTrue(result.contains("text"));
    }

    @Test
    void testConvertMarkdownToPlainText_MultipleParagraphs() {
        // Given
        String markdown = "Paragraph 1\n\nParagraph 2\n\n\nParagraph 3";

        // When
        String result = markdownService.convertMarkdownToPlainText(markdown);

        // Then
        assertEquals("Paragraph 1 Paragraph 2 Paragraph 3", result);
    }
}
