import javax.swing.*; // import used for GUI
import javax.swing.border.*; // import used for borders
import java.awt.*; // import used for GUI
import java.awt.event.*; // import used for event handling

import java.util.ArrayList; // import used for ArrayList

import java.io.*; // import used for reading and writing website responses/requests
import java.net.*; // import used for sending HTTP GET and POST requests to the server

import java.util.Calendar; // import used for retrieving current timestamps
import java.text.SimpleDateFormat; // import used for formatting timestamps

/**
 * The Screen class is responsible for nearly every function of the program except the login.
 * The Screen class instantiates a GUI with labels, boxes, and buttons.
 * Users, once their login is accepted by the Driver class, can enter into the text field box to search or post.
 * Users can also refresh to obtain the most recent posts.
 * Additionally, users may log out and head back to the beginning prompt, or they may directly exit from the program.
 * The Screen class utilizes GET and POST requests to a remote server for functionality. If the client cannot connect to the server, it will notify the user of the issue.
 * <br>
 * ---OTHER INFORMATION---<br>
 * @version 1.1
 * @since   2019-03-10
 */
public class Screen extends JPanel {
    private JLabel titleLabel; //the title text
    private JLabel userLabel; //the username text

    private JTextField box; //the multi-function text field box

    private JButton searchButton; //the search button
    private JButton postButton; //the post button
    private JButton refreshButton; //the refresh button
    private JButton logoutButton; //the logout button
    private JButton quitButton; //the logout button

    private JTextPane postsText; //the posts text

    private final String USER_AGENT; //the agent on which to send GET and POST requests
    private final String BASE_URL; // the base URL of the server
    private final String FORUM_NAME; // the name of the forums

    private String username; //the username of the user logged in

    /**
     * Constructor method which initiates the GUI and also sets the username in preparation for a POST request.
     * @param username the username of the user logged in
     */
    public Screen(String username, String userAgent, String baseUrl, String forumName) {
        USER_AGENT = userAgent;
        BASE_URL = baseUrl;
        FORUM_NAME = forumName;
        setLayout(new FlowLayout());

        //header - title and user
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        titleLabel = new JLabel(FORUM_NAME);
        titleLabel.setFont(new java.awt.Font("Serif", Font.BOLD | Font.ITALIC, 24));
        titleLabel.setForeground(new Color(175,35,35));
        header.add(titleLabel, BorderLayout.NORTH);
        userLabel = new JLabel("Currently logged in: " + username);
        userLabel.setFont(new java.awt.Font("Sans Serif", 1, 10));
        userLabel.setForeground(new Color(175,35,35));
        header.add(userLabel, BorderLayout.SOUTH);
        add(header);

        //multi-function textbox
        JPanel textbox = new JPanel();
        textbox.setLayout(new FlowLayout());
        box = new JTextField("", 50);
        box.setHorizontalAlignment(JTextField.LEFT);
        textbox.add(box);
        add(textbox);

        //buttons - search, post, refresh, logout, quit
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        searchButton = new JButton("Search");
        searchButton.addActionListener(new SearchListener());
        buttons.add(searchButton);
        postButton = new JButton("Post");
        postButton.addActionListener(new PostListener());
        buttons.add(postButton);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new RefreshListener());
        buttons.add(refreshButton);
        add(buttons);
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new LogOutListener());
        buttons.add(logoutButton);
        add(buttons);
        quitButton = new JButton("Quit");
        quitButton.addActionListener(new QuitListener());
        buttons.add(quitButton);
        add(buttons);

        //posts display setup
        JPanel postsBox = new JPanel();
        postsBox.setBorder(new TitledBorder(new EtchedBorder(), "Recent Posts"));
        postsBox.setBackground(new Color(255,240,240));
        postsText = new JTextPane();
        postsText.setContentType("text/html");
        postsText.setEditable(false);
        JScrollPane scroll = new JScrollPane(postsText);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        /*postsText = new JLabel("");
        postsText.setFont(new java.awt.Font("Monospaced", 1, 12));
        postsText.setForeground(Color.black);
        posts.add(postsText);*/
        postsBox.add(scroll);
        add(postsBox);

        //set the username
        this.username = username;

        //force button press to get new content
        refreshButton.doClick();
    }

    /**
     * Listens for an event from searchButton and acts upon it by filtering out posts for specific phrases.
     * Displays the posts in HTML format.
     * If there is an error in the retrieval of data, it will notify the user to try again later.
     */
    private class SearchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String[] posts = getPost().split("<br /><br />");
                ArrayList<String> matching = new ArrayList<>();
                for (String post : posts) {
                    if (post.contains(box.getText())) {
                        matching.add(post);
                    }
                }
                String filtered = ""; // posts in HTML format
                for (String post : matching) {
                    // append <br /><br /> to the end of each post as a line separator in HTML format
                    filtered += post + "<br /><br />";
                }
                // "convert" to HTML
                postsText.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", 400, filtered));
            } catch (Exception f) {
                JOptionPane.showMessageDialog(null, "Error in retrieving data. Try again later.", "Server Error", JOptionPane.OK_OPTION);
            }
        }
    }

    /**
     * Listens for an event from postButton and acts upon it by sending a POST request for the server to append to the posts database.
     * If there is no text in the box, this will take no action.
     * Clears any text in the text box after the post is successfully sent.
     * If there is an error in sending data, it will notify the user to try again later.
     */
    private class PostListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!box.getText().isEmpty()) {
                try{
                    sendPost(box.getText());
                    // if post is successful, refresh the posts
                    refreshButton.doClick();
                    // clear the box after post is send and posts are refreshed
                    box.setText("");
                } catch (Exception f){
                    JOptionPane.showMessageDialog(null, "Could not send post. Try again later.", "Server Error", JOptionPane.OK_OPTION);
                }
            }
        }
    }

    /**
     * Listens for an event from refreshButton and acts upon it by refreshing the posts label with recent posts from the posts database.
     * Displays the posts in HTML format.
     * Clears any text in the text box.
     * If there is an error in the retrieval of data, it will notify the user to try again later.
     */
    private class RefreshListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String getResult = getPost();
                if (getResult.equals("")) {
                    throw new Exception("e");
                }
                // "convert" to HTML
                postsText.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", 400, getResult));
            } catch (Exception f) {
                JOptionPane.showMessageDialog(null, "Error in retrieving data. Try again later.", "Server Error", JOptionPane.OK_OPTION);
            }
            // clear anything in the textbox
            box.setText("");
        }

    }

    /**
     * Listens for an event from logoutButton and acts upon it by logging the user out and redirecting them to the beginning prompt.
     */
    private class LogOutListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Driver.reboot();
        }
    }

    /**
     * Listens for an event from quitButton and acts upon it by closing the program entirely.
     */
    private class QuitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    /**
     * Sends a GET request to the server to get all posts from the posts database.
     * @return the String representation of all posts
     * @throws Exception an error in retrieval of data, typically due to server internal issues
     */
    private String getPost() throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(BASE_URL + "posts.txt");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        }
        return null;
    }

    /**
     * Sends a POST request to the server with information on the content of the post, the username of the poster, and the timestamp at which the post was sent.
     * The timestamp is calculated using the Calendar class, and formatted with SimpleDateFormat
     * @param post the String from the text box that is sent for the server to record
     * @throws Exception an error in sending data, typically due to server internal issues
     */
    private void sendPost(String post) throws Exception {
        URL url = new URL(BASE_URL + "api/post.php");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YY 'at' HH:mm:ss");
        String urlParameters = post + "\n-" + username + " " + sdf.format(cal.getTime()) + "\n\n";
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }
}
