import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Put your name here
 *
 */
public final class RSSAggregater {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregater() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        /*
         * while(!channel.child(i).label().equals(ttl)) { i++; }
         */
        int title = getChildElement(channel, "title");
        /*
         * while(!channel.child(j).label().equals("link")) { j++; }
         */
        int link = getChildElement(channel, "link");
        /*
         * while(!channel.child(d).label().equals("description")) { d++; }
         */
        int d = getChildElement(channel, "description");

        out.println("<html>");
        out.println("<head>");
        out.println(
                "<title>" + channel.child(title).child(0).label() + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("   <h1>");
        out.println("       <a href=" + channel.child(link).child(0).label()
                + ">" + channel.child(title).child(0).label() + "</a>");
        out.println("   </h1>");
        out.println("   <p>" + channel.child(d).child(0).label() + "</p>");
        out.println("<table border=\"1\">");
        out.println(" <tbody>");
        out.println("  <tr>");
        out.println("      <th>Date</th>");
        out.println("      <th>Source</th>");
        out.println("      <th>News</th>");
        out.println(" </tr>");

    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("  </tbody>");
        out.println(" </table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        int i = 0;
        int numOfChildren = xml.numberOfChildren();
        while (!xml.child(i).label().equals(tag)) {
            i++;
            if (i == numOfChildren) {
                return -1;
            }
        }
        return i;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        int date = getChildElement(item, "pubDate");
        int source = getChildElement(item, "source");
        int title = getChildElement(item, "title");
        int link = getChildElement(item, "link");
        String valueOfDate;
        String valueOfSource;
        String valueOfTitle = null;
        if (date == -1) {
            valueOfDate = "No date available";
        } else {
            valueOfDate = item.child(date).child(0).label();
        }
        if (source == -1) {
            valueOfSource = "No source available";
        } else {
            XMLTree sourceTag = item.child(source);
            valueOfSource = "<a href =" + sourceTag.attributeValue("url") + ">"
                    + item.child(source).child(0).label() + "</a>";
        }
        if (title == -1) {
            valueOfTitle = "No description available";
        } else {
            if (link == -1) {
                valueOfTitle = item.child(title).child(0).label();
            } else {
                XMLTree linkTag = item.child(link);
                valueOfTitle = "<a href =" + linkTag.child(0).label() + ">"
                        + item.child(title).child(0).label() + "</a>";
            }

        }
        out.println("<tr>");
        out.println("   <td>" + valueOfDate + "</td>");
        out.println("   <td>" + valueOfSource + "</td>");
        out.println("   <td>" + valueOfTitle + "</td>");
        out.println("</tr>");

    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
     */
    private static void processFeed(String url, String file, SimpleWriter out) {
        out = new SimpleWriter1L("index.html");
        XMLTree xml = new XMLTree1(url);
        String urlContent;
        String name;
        file = xml.attributeValue("file");
        if (xml.hasAttribute("url")) {
            urlContent = xml.attributeValue("url");
        } else {
            urlContent = "no available url";
        }
        if (xml.hasAttribute("name")) {
            name = xml.attributeValue("name");
        } else {
            name = "no available name";
        }
        out.println("<li>");
        out.println("   <a href=" + file + ">" + name + "</a>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L("index.html");
        SimpleWriter out1 = new SimpleWriter1L();
        out1.println("input valid URL");
        String xt = in.nextLine();
        XMLTree xml = new XMLTree1(xt);

        /*
         * determine whether this url is a rss 2.0 feed
         */

        /*
         * if (xml.label().equals("RSS")) { String value = "2.0"; if
         * (xml.attributeValue("version").equals(value)) {
         */
        XMLTree channel = xml.child(0);
        int numOfItem = channel.numberOfChildren();
        outputHeader(channel, out);
        for (int i = 0; i < numOfItem; i++) {
            XMLTree child = channel.child(i);
            if (child.label().equals("item")) {
                processItem(xml.child(0).child(i), out);
            }
        }
        outputFooter(out);
        /*
         * } else { System.exit(0); } } else { System.exit(0); }
         */
        in.close();
        out.close();
    }

}

/*
 * XMLTree xml = new XMLTree1(url); int numOfFeeds = xml.numberOfChildren();
 * 
 * String title = null; out.println("<html>"); out.println(" <head>"); if
 * (xml.hasAttribute("title")) { title = xml.attributeValue("title"); } else {
 * title = "no title"; } out.println("   <titile>" + title + "</title>");
 * out.println(" </head>"); out.println(" <body>"); out.println("   <h2>" +
 * title + "</h2>"); out.println(" <ul>");
 */