package com.itextpdf.samples.book.part2.chapter07;

import com.itextpdf.basics.geom.Rectangle;
import com.itextpdf.basics.image.ImageFactory;
import com.itextpdf.core.pdf.PdfDocument;
import com.itextpdf.core.pdf.PdfName;
import com.itextpdf.core.pdf.PdfReader;
import com.itextpdf.core.pdf.PdfString;
import com.itextpdf.core.pdf.PdfWriter;
import com.itextpdf.core.pdf.action.PdfAction;
import com.itextpdf.core.pdf.annot.PdfAnnotation;
import com.itextpdf.core.pdf.annot.PdfPopupAnnotation;
import com.itextpdf.core.pdf.annot.PdfTextAnnotation;
import com.itextpdf.core.testutils.annotations.type.SampleTest;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.model.element.Image;
import com.itextpdf.samples.GenericTest;
import com.lowagie.database.DatabaseConnection;
import com.lowagie.database.HsqldbConnection;
import com.lowagie.filmfestival.Movie;
import com.lowagie.filmfestival.PojoFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Ignore;
import org.junit.experimental.categories.Category;

@Ignore
@Category(SampleTest.class)
public class Listing_07_28_MoviePosters2 extends GenericTest {
    public static final String DEST = "./target/test/resources/book/part2/chapter07/Listing_07_28_MoviePosters2.pdf";
    /** A pattern for an info string. */
    public static final String INFO = "Movie produced in %s; run length: %s";
    /** A JavaScript snippet */
    public static final String JS1 =
            "var t = this.getAnnot(this.pageNum, 'IMDB%1$s'); t.popupOpen = true; "
                    + "var w = this.getField('b%1$s'); w.setFocus();";
    /** A JavaScript snippet */
    public static final String JS2 =
            "var t = this.getAnnot(this.pageNum, 'IMDB%s'); t.popupOpen = false;";

    protected String[] arguments;

    public static final String MOVIE_POSTERS1 = "./src/test/resources/book/part2/chapter07/cmp_Listing_07_22_MoviePosters1.pdf";

    public static void main(String args[]) throws IOException, SQLException {
        Listing_07_28_MoviePosters2 application = new Listing_07_28_MoviePosters2();
        application.arguments = args;
        application.manipulatePdf(DEST);
    }

    public void manipulatePdf(String dest) throws IOException, SQLException {
        // Listing_07_22_MoviePosters1.main(arguments);
        // Create a database connection
        DatabaseConnection connection = new HsqldbConnection("filmfestival");
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(MOVIE_POSTERS1), new PdfWriter(DEST));
        // Loop over all the movies to add a popup annotation
        List<Movie> movies = PojoFactory.getMovies(connection);
        Image img;
        float x = 11.5f;
        float y = 769.7f;
        float llx, lly, urx, ury;
        for (Movie movie : movies) {
            img = new Image(ImageFactory.getImage(String.format(Listing_07_22_MoviePosters1.RESOURCE,
                    movie.getImdb())));
            img.scaleToFit(1000, 60);
            llx = x + (45 - img.getImageScaledWidth()) / 2;
            lly = y;
            urx = x + img.getImageScaledWidth();
            ury = y + img.getImageScaledHeight();
            addPopup(pdfDoc, new Rectangle(llx, lly, urx-llx, ury-lly),
                    movie.getMovieTitle(),
                    String.format(INFO, movie.getYear(), movie.getDuration()), movie.getImdb());
            x += 48;
            if (x > 578) {
                x = 11.5f;
                y -= 84.2f;
            }
        }
        // Close the stamper
        pdfDoc.close();
        // Close the database connection
        connection.close();
    }

    /**
     * Adds a popup.
     * @param pdfDoc the PdfStamper to which the annotation needs to be added
     * @param rect the position of the annotation
     * @param title the annotation title
     * @param contents the annotation content
     * @param imdb the IMDB number of the movie used as name of the annotation
     * @throws IOException
     */
    public void addPopup(PdfDocument pdfDoc, Rectangle rect,
                         String title, String contents, String imdb)
            throws IOException{
        // Create the text annotation
        PdfAnnotation text = new PdfTextAnnotation(pdfDoc, rect)
                .setIconName(new PdfName("Comment"))
                .setTitle(new PdfString(title))
                .setContents(contents)
                .setOpen(false)
                .setName(new PdfString(String.format("IMDB%s", imdb)));
        text.setFlags(PdfAnnotation.ReadOnly | PdfAnnotation.NoView);
        // Create the popup annotation
        PdfAnnotation popup = new PdfPopupAnnotation(pdfDoc,
                new Rectangle(rect.getLeft() + 10, rect.getBottom() + 10, 190, 90));
        // Add the text annotation to the popup
        popup.put(PdfName.Parent, text.getPdfObject().getIndirectReference());
        // Declare the popup annotation as popup for the text
        text.put(PdfName.Popup, popup.getPdfObject().getIndirectReference());
        // Add both annotations
        pdfDoc.getPage(1).addAnnotation(text);
        pdfDoc.getPage(1).addAnnotation(popup);
        // TODO No facility to make field transparent
        // Create a button field
        PdfButtonFormField field = PdfFormField.createPushButton(pdfDoc, rect, String.format("b%s", imdb), "");
        PdfAnnotation widget = field.getWidgets().get(0);
        // Show the popup onMouseEnter
        PdfAction enter = PdfAction.createJavaScript(pdfDoc, String.format(JS1, imdb));
        widget.setAdditionalAction(PdfName.E, enter);
        // Hide the popup onMouseExit
        PdfAction exit = PdfAction.createJavaScript(pdfDoc, String.format(JS2, imdb));
        widget.setAdditionalAction(PdfName.X, exit);
        // Add the button annotation
        pdfDoc.getPage(1).addAnnotation(widget);
    }
}