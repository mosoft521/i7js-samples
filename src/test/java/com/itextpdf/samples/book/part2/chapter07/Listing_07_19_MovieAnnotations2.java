package com.itextpdf.samples.book.part2.chapter07;

import com.itextpdf.basics.geom.Rectangle;
import com.itextpdf.canvas.PdfCanvas;
import com.itextpdf.core.pdf.PdfDocument;
import com.itextpdf.core.pdf.PdfString;
import com.itextpdf.core.pdf.PdfWriter;
import com.itextpdf.core.pdf.annot.PdfAnnotation;
import com.itextpdf.core.pdf.annot.PdfTextAnnotation;
import com.itextpdf.core.testutils.annotations.type.SampleTest;
import com.itextpdf.model.Document;
import com.itextpdf.model.element.Paragraph;
import com.itextpdf.model.element.Text;
import com.itextpdf.model.renderer.TextRenderer;
import com.itextpdf.samples.GenericTest;
import com.lowagie.database.DatabaseConnection;
import com.lowagie.database.HsqldbConnection;
import com.lowagie.filmfestival.Movie;
import com.lowagie.filmfestival.PojoFactory;
import com.lowagie.filmfestival.PojoToElementFactory;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.experimental.categories.Category;

@Category(SampleTest.class)
public class Listing_07_19_MovieAnnotations2 extends GenericTest {
    public static final String DEST
            = "./target/test/resources/book/part2/chapter07/Listing_07_19_MovieAnnotations2.pdf";
    /** Pattern for an info String. */
    public static final String INFO
            = "Movie produced in %s; run length: %s";

    protected String[] arguments;

    public static void main(String args[]) throws IOException, SQLException {
        new Listing_07_19_MovieAnnotations2().manipulatePdf(DEST);
    }

    public void manipulatePdf(String dest) throws IOException, SQLException {
        // Open the database connection
        DatabaseConnection connection = new HsqldbConnection("filmfestival");

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
        Document doc = new Document(pdfDoc);

        Paragraph p;
        Text text;
        for (Movie movie : PojoFactory.getMovies(connection)) {
            p = new Paragraph(movie.getMovieTitle());
            text = new Text("\u00a0");
            text.setNextRenderer(new AnnotatedTextRenderer(text, movie.getMovieTitle(),
                    String.format(INFO, movie.getYear(), movie.getDuration())));
            p.add(text);
            doc.add(p);
            doc.add(PojoToElementFactory.getDirectorList(movie));
            doc.add(PojoToElementFactory.getCountryList(movie));
        }
        doc.close();

        // close the database connection
        connection.close();
    }


    private class AnnotatedTextRenderer extends TextRenderer {
        private String contents;
        private String title;

        public AnnotatedTextRenderer(Text textElement, String title, String contents) {
            super(textElement);
            this.title = title;
            this.contents = contents;
        }

        @Override
        public void draw(PdfDocument document, PdfCanvas canvas) {
            super.draw(document, canvas);
            Rectangle rect = new Rectangle(getOccupiedAreaBBox().getLeft() + getOccupiedAreaBBox().getWidth() / 4,
                    getOccupiedAreaBBox().getBottom(), 10, 10);
            PdfAnnotation annotation = new PdfTextAnnotation(document, rect)
                    .setTitle(new PdfString(title))
                    .setContents(new PdfString(contents))
                    .setOpen(false)
                    .setName(new PdfString("Comment"));
            document.getLastPage().addAnnotation(annotation);
        }
    }
}
