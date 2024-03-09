import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JavaFxXmlClipboardTutorial {

    private static final DocumentBuilderFactory XML_DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private static DataFormat XML_SPREADSHEET_DATAFORMAT;

    public static void readXmlSpreadsheetFromClipboard() {
        final var clipboard = Clipboard.getSystemClipboard();
        final var clipboardContent = clipboard.getContent(XML_SPREADSHEET_DATAFORMAT);

        if (clipboardContent == null) {
            /*
            If the clipboard does not contain an XML Spreadsheet,
            Clipboard::getContent(DataFormat) will return null.
            */
            return;
        }

        final var clipboardBytes = ((ByteBuffer) clipboardContent).array();

        /*
        It's crucial to remove the trailing nul byte as it causes java.xml parsers to throw
        fatal exceptions, and ones I found quite obscure.
         */
        final var clipboardBytesWithoutNulByte = Arrays.copyOfRange(
                clipboardBytes,
                0,
                clipboardBytes.length - 1
        );

        try {
            final var builder = XML_DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final var document = builder.parse(new ByteArrayInputStream(clipboardBytesWithoutNulByte));

            /*
            Now you have an XML DOM that contains the Elements and Attributes of the XML Spreadsheet.
            For example, you can invoke Document::getDocumentElement() to obtain the Workbook element
            containing all other elements.

            I have not been able to find a specification for this format, as it differs both
            from Office Open XML and SpreadsheetML. Thankfully, it is easy to inspect the XML Spreadsheet
            content in XML form by invoking:

            System.out.println(new String(clipboardBytes, StandardCharsets.UTF_8));
             */

        } catch (Exception ignored) {
            // Maybe notify the user that the XML Spreadsheet was malformed?
        }
    }

    static {
        XML_DOCUMENT_BUILDER_FACTORY.setValidating(false);

        /*
        JavaFX will create the corresponding DataFormat the first time a user tries to
        paste an XML Spreadsheet into your application. In this case, "XML Spreadsheet"
        is what Microsoft Excel calls the XML clipboard contents it puts into the clipboard
        when the user copies a table.
         */
        XML_SPREADSHEET_DATAFORMAT = DataFormat.lookupMimeType("XML Spreadsheet");

        /*
        A DataFormat is merely the name of a protocol; it's perfectly valid to create the
        DataFormat you want if it doesn't exist yet in the application.
         */
        if (XML_SPREADSHEET_DATAFORMAT == null) {
            XML_SPREADSHEET_DATAFORMAT = new DataFormat("XML Spreadsheet");
        }
    }
}
