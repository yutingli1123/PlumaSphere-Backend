package fans.goldenglow.plumaspherebackend.service;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

/**
 * Service for converting Markdown text to plain text.
 * This service uses Flexmark for parsing Markdown and Jsoup for extracting plain text.
 */
@Service
public class MarkdownService {

    /**
     * Converts Markdown formatted text to plain text.
     *
     * @param markdown the Markdown text to convert
     * @return the plain text extracted from the Markdown
     */
    public String convertMarkdownToPlainText(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Document document = parser.parse(markdown);
        String html = renderer.render(document);

        String plainText = Jsoup.parse(html).text();
        return plainText.replaceAll("\\r\\n|\\r|\\n", " ");
    }
}
